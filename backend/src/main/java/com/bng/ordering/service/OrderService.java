package com.bng.ordering.service;

import com.bng.ordering.model.request.CreateOrderRequest;
import com.bng.ordering.model.response.OrderResponse;
import com.clover.sdk.CloverClient;
import com.clover.sdk.PaymentType;
import com.clover.sdk.exception.CloverException;
import com.clover.sdk.model.request.OrderItem;
import com.clover.sdk.model.request.OrderRequest;
import com.clover.sdk.model.response.Customer;
import com.clover.sdk.model.response.LineItem;
import com.clover.sdk.model.response.Order;
import com.clover.sdk.model.response.OrderResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final CloverClient cloverClient;
    private final PaymentService paymentService;

    public OrderService(CloverClient cloverClient, PaymentService paymentService) {
        this.cloverClient = cloverClient;
        this.paymentService = paymentService;
    }

    public OrderResponse createOrder(CreateOrderRequest req) {
        List<OrderItem> sdkItems = req.getItems().stream()
            .map(item -> OrderItem.builder()
                .id(item.getItemId())
                .name(item.getName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build())
            .collect(Collectors.toList());

        boolean isPrepaid = "PREPAID".equalsIgnoreCase(req.getPaymentType());

        // Charge the card BEFORE creating the order — if charge fails, order is not created
        String chargeId = null;
        if (isPrepaid) {
            if (req.getCardToken() == null || req.getCardToken().isBlank()) {
                throw new RuntimeException("Card token is required for online payment");
            }
            long totalCents = req.getItems().stream()
                .mapToLong(i -> i.getPrice() * i.getQuantity())
                .sum();
            long taxCents = Math.round(totalCents * 0.07);
            chargeId = paymentService.chargeCard(totalCents + taxCents, req.getCardToken());
        }

        PaymentType paymentType = isPrepaid
            ? PaymentType.PREPAID
            : PaymentType.PAY_AT_RESTAURANT;

        OrderRequest.Builder sdkReqBuilder = OrderRequest.builder()
            .items(sdkItems)
            .customerName(req.getCustomerName())
            .paymentType(paymentType);

        if (req.getCustomerPhone() != null) sdkReqBuilder.customerPhone(req.getCustomerPhone());
        if (req.getNotes() != null)         sdkReqBuilder.orderNote(req.getNotes());

        OrderResult result;
        try {
            result = cloverClient.placeOrder(sdkReqBuilder.build());
        } catch (CloverException e) {
            throw new RuntimeException("Failed to place order on Clover: " + e.getMessage(), e);
        }

        return toOrderResponse(result, req, chargeId);
    }

    public Optional<OrderResponse> getOrder(String orderId) {
        try {
            Order order = cloverClient.getOrder(orderId);
            return Optional.of(toOrderResponse(order));
        } catch (CloverException e) {
            if (e.getHttpStatus() == 404) return Optional.empty();
            throw new RuntimeException("Failed to fetch order from Clover: " + e.getMessage(), e);
        }
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private OrderResponse toOrderResponse(OrderResult result, CreateOrderRequest req, String chargeId) {
        long totalCents = req.getItems().stream()
            .mapToLong(i -> i.getPrice() * i.getQuantity())
            .sum();
        double subtotal = totalCents / 100.0;
        double tax      = Math.round(subtotal * 0.08 * 100.0) / 100.0;
        double total    = Math.round((subtotal + tax) * 100.0) / 100.0;

        List<OrderResponse.OrderLineItem> lineItems = req.getItems().stream()
            .map(item -> OrderResponse.OrderLineItem.builder()
                .itemId(item.getItemId())
                .name(item.getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getPrice() / 100.0)
                .lineTotal(item.getPrice() * item.getQuantity() / 100.0)
                .specialInstructions(item.getSpecialInstructions())
                .build())
            .collect(Collectors.toList());

        String status = result.isPrepaidSuccessful() ? "COMPLETED" : "CONFIRMED";

        return OrderResponse.builder()
            .orderId(result.getOrderId())
            .chargeId(chargeId)
            .status(status)
            .customerName(req.getCustomerName())
            .customerPhone(req.getCustomerPhone())
            .items(lineItems)
            .subtotal(subtotal)
            .tax(tax)
            .total(total)
            .estimatedReadyIn("25-35 minutes")
            .createdAt(Instant.now())
            .build();
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderResponse.OrderLineItem> lineItems = order.getLineItemList().stream()
            .filter(li -> li.getPrice() != null && li.getPrice() > 0)
            .map(li -> OrderResponse.OrderLineItem.builder()
                .itemId(li.getId())
                .name(li.getName())
                .quantity(1)
                .unitPrice(li.getPrice() / 100.0)
                .lineTotal(li.getPrice() / 100.0)
                .build())
            .collect(Collectors.toList());

        double subtotal = lineItems.stream().mapToDouble(OrderResponse.OrderLineItem::getLineTotal).sum();
        double tax      = Math.round(subtotal * 0.08 * 100.0) / 100.0;
        double total    = Math.round((subtotal + tax) * 100.0) / 100.0;

        String customerName  = null;
        String customerPhone = null;
        if (!order.getCustomerList().isEmpty()) {
            Customer cust  = order.getCustomerList().get(0);
            customerName   = cust.getFullName();
            customerPhone  = cust.getFirstPhoneNumber();
        }

        return OrderResponse.builder()
            .orderId(order.getId())
            .status(mapCloverState(order.getState()))
            .customerName(customerName)
            .customerPhone(customerPhone)
            .items(lineItems)
            .subtotal(subtotal)
            .tax(tax)
            .total(total)
            .createdAt(order.getCreatedTime() != null
                ? Instant.ofEpochMilli(order.getCreatedTime()) : Instant.now())
            .build();
    }

    private String mapCloverState(String state) {
        if (state == null) return "CONFIRMED";
        return switch (state.toLowerCase()) {
            case "open"   -> "CONFIRMED";
            case "locked" -> "PREPARING";
            case "paid"   -> "COMPLETED";
            default       -> "CONFIRMED";
        };
    }
}
