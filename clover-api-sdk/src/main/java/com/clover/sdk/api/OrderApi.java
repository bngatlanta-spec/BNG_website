package com.clover.sdk.api;

import com.clover.sdk.PaymentType;
import com.clover.sdk.exception.CloverException;
import com.clover.sdk.internal.CloverHttpClient;
import com.clover.sdk.model.request.OrderItem;
import com.clover.sdk.model.request.OrderRequest;
import com.clover.sdk.model.response.Customer;
import com.clover.sdk.model.response.Order;
import com.clover.sdk.model.response.OrderResult;
import com.clover.sdk.model.response.Payment;
import com.clover.sdk.model.response.PrintEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

public class OrderApi {

    private final CloverHttpClient http;
    private final MerchantApi merchantApi;

    // Simple TTL cache to avoid redundant lookups within a session
    private final Map<String, CacheEntry<String>> cache = new HashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60_000L;

    public OrderApi(CloverHttpClient http, MerchantApi merchantApi) {
        this.http = http;
        this.merchantApi = merchantApi;
    }

    /**
     * Places an order on Clover, following the exact flow used in production:
     * <ol>
     *   <li>Create order</li>
     *   <li>Create Clover customer record and link it to the order</li>
     *   <li>Add header line items (customer name/phone printed at top of kitchen ticket)</li>
     *   <li>Add food line items</li>
     *   <li>Lock the order</li>
     *   <li>Fire print_event to trigger kitchen printing</li>
     *   <li>If PREPAID: record a payment against the order</li>
     *   <li>Fetch the completed order for the result</li>
     * </ol>
     *
     * @throws CloverException if the order creation itself fails (post-creation failures are non-fatal)
     */
    public OrderResult placeOrder(OrderRequest request) throws CloverException {
        String merchantId = http.getMerchantId();
        String base = "/v3/merchants/" + merchantId;

        // Resolve employee and order type (cached)
        String employeeId = cached("emp", () -> merchantApi.resolveEmployeeId());
        String orderTypeId = request.getOrderTypeId() != null
                ? request.getOrderTypeId()
                : cached("otype", () -> merchantApi.resolveOrderTypeId());

        // Build order note
        String fullNote = buildNote(request);

        // 1. Create the order
        ObjectNode orderPayload = http.mapper.createObjectNode();
        orderPayload.put("state", "open");
        orderPayload.put("manualTransaction", false);
        orderPayload.put("groupLineItems", false);
        orderPayload.put("clientCreatedTime", System.currentTimeMillis());
        orderPayload.put("note", fullNote);
        if (employeeId != null) {
            orderPayload.putObject("employee").put("id", employeeId);
        }
        if (orderTypeId != null) {
            orderPayload.putObject("orderType").put("id", orderTypeId);
        }

        JsonNode orderNode = http.post(base + "/orders", orderPayload);
        String orderId;
        try {
            orderId = orderNode.get("id").asText();
        } catch (Exception e) {
            throw new CloverException("Clover returned an order without an ID — order may not have been created");
        }

        // 2. Create customer record and link to order
        Customer customer = createAndLinkCustomer(base, orderId, request);

        // 3. Add header line items (name/phone as $0 items — fallback visible on kitchen ticket)
        addHeaderLineItems(base, orderId, request);

        // 4. Add food line items
        for (OrderItem item : request.getItems()) {
            ObjectNode lineItemPayload = http.mapper.createObjectNode();
            lineItemPayload.putObject("item").put("id", item.getId());
            lineItemPayload.put("name", item.getName());
            lineItemPayload.put("price", item.getPrice());
            for (int i = 0; i < item.getQuantity(); i++) {
                http.post(base + "/orders/" + orderId + "/line_items", lineItemPayload);
            }
        }

        // 5. Lock the order
        ObjectNode lockPayload = http.mapper.createObjectNode();
        lockPayload.put("state", "locked");
        http.post(base + "/orders/" + orderId, lockPayload);

        // 6. Fire print event (non-fatal)
        PrintEvent printEvent = firePrintEvent(base, orderId);

        // 7. Record payment if prepaid (non-fatal — order exists even if this fails)
        Payment payment = null;
        String paymentFailedReason = null;
        if (request.getPaymentType() == PaymentType.PREPAID) {
            try {
                payment = recordPrepaidPayment(base, orderId, request);
            } catch (CloverException e) {
                paymentFailedReason = e.getMessage();
            }
        }

        // 8. Fetch the updated order
        Order updatedOrder = fetchOrder(orderId);

        String orderState;
        if (request.getPaymentType() == PaymentType.PREPAID) {
            orderState = (payment != null)
                    ? "paid"
                    : "open (payment not recorded — token may lack Payments permission)";
        } else {
            orderState = "open (pay at restaurant)";
        }

        return OrderResult.builder()
                .success(true)
                .orderId(orderId)
                .orderState(orderState)
                .paymentType(request.getPaymentType())
                .order(updatedOrder)
                .payment(payment)
                .printEvent(printEvent)
                .customer(customer)
                .paymentFailedReason(paymentFailedReason)
                .build();
    }

    /**
     * Fetches a single order by ID, expanded with line items and customers.
     */
    public Order getOrder(String orderId) throws CloverException {
        return fetchOrder(orderId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String buildNote(OrderRequest request) {
        StringBuilder sb = new StringBuilder("Online Order");
        if (request.getCustomerName() != null) sb.append(" | Customer: ").append(request.getCustomerName());
        if (request.getCustomerPhone() != null) sb.append(" | Phone: ").append(request.getCustomerPhone());
        if (request.getOrderNote() != null && !request.getOrderNote().isBlank()) {
            sb.append(" | ").append(request.getOrderNote());
        }
        return sb.toString();
    }

    private Customer createAndLinkCustomer(String base, String orderId, OrderRequest request) {
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) return null;
        try {
            String[] nameParts = request.getCustomerName().trim().split("\\s+", 2);
            ObjectNode custPayload = http.mapper.createObjectNode();
            custPayload.put("firstName", nameParts[0]);
            custPayload.put("lastName", nameParts.length > 1 ? nameParts[1] : "");
            if (request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank()) {
                custPayload.putObject("phoneNumbers")
                        .putArray("elements")
                        .addObject()
                        .put("phoneNumber", request.getCustomerPhone());
            }

            JsonNode custNode = http.post(base + "/customers", custPayload);
            String customerId = custNode.path("id").asText(null);
            Customer customer = http.mapper.treeToValue(custNode, Customer.class);

            if (customerId != null) {
                // Attempt to link — Method 1: bare POST to relationship endpoint
                try {
                    http.postEmpty(base + "/orders/" + orderId + "/customers/" + customerId);
                } catch (CloverException e1) {
                    // Method 2 fallback: PATCH the order with customer reference
                    try {
                        ObjectNode link = http.mapper.createObjectNode();
                        link.putObject("customers").putArray("elements").addObject().put("id", customerId);
                        http.post(base + "/orders/" + orderId, link);
                    } catch (CloverException e2) {
                        // Non-fatal: order still exists, customer just isn't linked
                    }
                }
            }
            return customer;
        } catch (Exception e) {
            return null;
        }
    }

    private void addHeaderLineItems(String base, String orderId, OrderRequest request) {
        String[] headers = {
                "Online Order",
                request.getCustomerName(),
                request.getCustomerPhone()
        };
        for (String header : headers) {
            if (header == null || header.isBlank()) continue;
            try {
                ObjectNode payload = http.mapper.createObjectNode();
                payload.put("name", header);
                payload.put("price", 0);
                http.post(base + "/orders/" + orderId + "/line_items", payload);
            } catch (CloverException e) {
                // Non-fatal
            }
        }
    }

    private PrintEvent firePrintEvent(String base, String orderId) {
        try {
            ObjectNode payload = http.mapper.createObjectNode();
            payload.putObject("orderRef").put("id", orderId);
            JsonNode node = http.post(base.replace("/v3/merchants/" + http.getMerchantId(), "")
                    + "/v3/merchants/" + http.getMerchantId() + "/print_event", payload);
            PrintEvent event = new PrintEvent();
            event.setId(node.path("id").asText(null));
            event.setState(node.path("state").asText(null));
            return event;
        } catch (CloverException e) {
            PrintEvent event = new PrintEvent();
            event.setError(e.getMessage());
            return event;
        }
    }

    private Payment recordPrepaidPayment(String base, String orderId, OrderRequest request) throws CloverException {
        String tenderId = merchantApi.resolveTenderId();
        String employeeId = merchantApi.resolveEmployeeId();
        long total = request.getTotalCents();

        ObjectNode payload = http.mapper.createObjectNode();
        payload.putObject("order").put("id", orderId);
        payload.putObject("tender").put("id", tenderId);
        payload.put("amount", total);
        payload.put("tipAmount", 0);
        payload.put("cashTendered", total);
        payload.put("clientCreatedTime", System.currentTimeMillis());
        if (employeeId != null) {
            payload.putObject("employee").put("id", employeeId);
        }

        JsonNode node = http.post(base + "/orders/" + orderId + "/payments", payload);
        try {
            return http.mapper.treeToValue(node, Payment.class);
        } catch (Exception e) {
            throw new CloverException("Failed to parse payment response", e);
        }
    }

    private Order fetchOrder(String orderId) throws CloverException {
        String base = "/v3/merchants/" + http.getMerchantId();
        // Try with customers expand first, fall back if permission denied
        try {
            JsonNode node = http.get(base + "/orders/" + orderId + "?expand=lineItems,customers");
            return http.mapper.treeToValue(node, Order.class);
        } catch (CloverException e1) {
            try {
                JsonNode node = http.get(base + "/orders/" + orderId + "?expand=lineItems");
                return http.mapper.treeToValue(node, Order.class);
            } catch (CloverException e2) {
                try {
                    JsonNode node = http.get(base + "/orders/" + orderId);
                    return http.mapper.treeToValue(node, Order.class);
                } catch (Exception e3) {
                    throw new CloverException("Failed to fetch order " + orderId, e3);
                }
            } catch (Exception e) {
                throw new CloverException("Failed to parse order response", e);
            }
        } catch (Exception e) {
            throw new CloverException("Failed to parse order response", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T cached(String key, CacheSupplier<T> supplier) {
        CacheEntry<String> entry = cache.get(key);
        if (entry != null && System.currentTimeMillis() < entry.expiry) {
            return (T) entry.value;
        }
        T value;
        try {
            value = supplier.get();
        } catch (Exception e) {
            value = null;
        }
        cache.put(key, new CacheEntry<>((String) value, System.currentTimeMillis() + CACHE_TTL_MS));
        return value;
    }

    @FunctionalInterface
    private interface CacheSupplier<T> {
        T get() throws Exception;
    }

    private static class CacheEntry<T> {
        final T value;
        final long expiry;
        CacheEntry(T value, long expiry) { this.value = value; this.expiry = expiry; }
    }
}
