package com.clover.sdk;

import com.clover.sdk.api.MenuApi;
import com.clover.sdk.api.MerchantApi;
import com.clover.sdk.api.OrderApi;
import com.clover.sdk.exception.CloverException;
import com.clover.sdk.internal.CloverHttpClient;
import com.clover.sdk.model.request.OrderRequest;
import com.clover.sdk.model.response.Category;
import com.clover.sdk.model.response.Employee;
import com.clover.sdk.model.response.Menu;
import com.clover.sdk.model.response.Merchant;
import com.clover.sdk.model.response.MenuItem;
import com.clover.sdk.model.response.ModifierGroup;
import com.clover.sdk.model.response.Order;
import com.clover.sdk.model.response.OrderResult;
import com.clover.sdk.model.response.OrderType;
import com.clover.sdk.model.response.Tender;

import java.util.List;

/**
 * Main entry point for the Clover API SDK.
 *
 * <h3>Quick start</h3>
 * <pre>
 * CloverConfig config = CloverConfig.builder()
 *     .merchantId("YOUR_MERCHANT_ID")
 *     .token("YOUR_API_TOKEN")
 *     .environment(CloverEnvironment.SANDBOX)   // or PRODUCTION
 *     .build();
 *
 * CloverClient client = new CloverClient(config);
 *
 * // Get the menu
 * List&lt;MenuItem&gt; items = client.getAvailableMenuItems();
 *
 * // Place an order (pay at restaurant)
 * OrderRequest request = OrderRequest.builder()
 *     .addItem(OrderItem.builder().id(items.get(0).getId())
 *         .name(items.get(0).getName())
 *         .price(items.get(0).getPrice())
 *         .quantity(1)
 *         .build())
 *     .customerName("Jane Smith")
 *     .paymentType(PaymentType.PAY_AT_RESTAURANT)
 *     .build();
 *
 * OrderResult result = client.placeOrder(request);
 * System.out.println("Order ID: " + result.getOrderId());
 * </pre>
 */
public class CloverClient {

    private final CloverConfig config;
    private final MerchantApi merchantApi;
    private final MenuApi menuApi;
    private final OrderApi orderApi;

    public CloverClient(CloverConfig config) {
        this.config = config;
        CloverHttpClient http = new CloverHttpClient(config);
        this.merchantApi = new MerchantApi(http);
        this.menuApi = new MenuApi(http);
        this.orderApi = new OrderApi(http, merchantApi);
    }

    // ── Merchant ─────────────────────────────────────────────────────────────

    /**
     * Returns the merchant's profile (name, address, etc.).
     */
    public Merchant getMerchant() throws CloverException {
        return merchantApi.getMerchant();
    }

    // ── Menu ─────────────────────────────────────────────────────────────────

    /**
     * Returns all menus configured for this merchant.
     */
    public List<Menu> getMenus() throws CloverException {
        return menuApi.getMenus();
    }

    /**
     * Returns all modifier groups with their individual modifiers (options + prices).
     */
    public List<ModifierGroup> getModifierGroupsWithModifiers() throws CloverException {
        return menuApi.getModifierGroupsWithModifiers();
    }

    /**
     * Returns all menu categories, each containing references to the items in that category.
     */
    public List<Category> getCategories() throws CloverException {
        return menuApi.getCategories();
    }

    /**
     * Returns all menu items, including hidden/deleted ones. Use {@link #getAvailableMenuItems()}
     * for a filtered list ready to display.
     */
    public List<MenuItem> getMenuItems() throws CloverException {
        return menuApi.getMenuItems();
    }

    /**
     * Returns only items that are visible and available for ordering.
     */
    public List<MenuItem> getAvailableMenuItems() throws CloverException {
        return menuApi.getAvailableMenuItems();
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    /**
     * Places an order on Clover.
     *
     * <p>Supports two payment modes via {@link OrderRequest#getPaymentType()}:
     * <ul>
     *   <li>{@link PaymentType#PAY_AT_RESTAURANT} — order is created as open, customer pays at the counter.</li>
     *   <li>{@link PaymentType#PREPAID} — order is created and then marked as paid.
     *       Requires the <em>Payments</em> permission on the API token.</li>
     * </ul>
     *
     * @param request the order details
     * @return an {@link OrderResult} with the order ID, state, payment info, and print event status
     * @throws CloverException if the order could not be created
     */
    public OrderResult placeOrder(OrderRequest request) throws CloverException {
        return orderApi.placeOrder(request);
    }

    /**
     * Fetches an existing order by its Clover order ID.
     */
    public Order getOrder(String orderId) throws CloverException {
        return orderApi.getOrder(orderId);
    }

    // ── Supporting data ───────────────────────────────────────────────────────

    /**
     * Returns the merchant's configured order types (dine-in, take-out, etc.).
     */
    public List<OrderType> getOrderTypes() throws CloverException {
        return merchantApi.getOrderTypes();
    }

    /**
     * Returns the merchant's tenders (payment methods).
     */
    public List<Tender> getTenders() throws CloverException {
        return merchantApi.getTenders();
    }

    /**
     * Returns the merchant's employees. Requires the Employees permission on the token.
     */
    public List<Employee> getEmployees() throws CloverException {
        return merchantApi.getEmployees();
    }

    /**
     * Charges a card via Clover Ecommerce.
     * @param amountCents total amount in cents
     * @param cardToken   token from Clover.js (frontend tokenization)
     * @return charge result JSON node containing id and status
     */
    public com.fasterxml.jackson.databind.JsonNode chargeCard(long amountCents, String cardToken)
            throws CloverException {
        CloverHttpClient http = new CloverHttpClient(config);
        return http.chargeCard(amountCents, cardToken);
    }

    public CloverConfig getConfig() {
        return config;
    }
}
