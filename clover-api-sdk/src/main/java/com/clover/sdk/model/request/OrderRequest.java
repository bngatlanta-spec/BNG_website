package com.clover.sdk.model.request;

import com.clover.sdk.PaymentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Request object for placing an order via {@code CloverClient.placeOrder()}.
 *
 * <pre>
 * OrderRequest request = OrderRequest.builder()
 *     .addItem(OrderItem.builder().id("ITEM_ID").name("Burger").price(1299).quantity(2).build())
 *     .customerName("Jane Smith")
 *     .customerPhone("5551234567")
 *     .orderNote("No onions")
 *     .paymentType(PaymentType.PAY_AT_RESTAURANT)
 *     .build();
 * </pre>
 */
public class OrderRequest {

    private final List<OrderItem> items;
    private final PaymentType paymentType;
    private final String customerName;
    private final String customerPhone;
    private final String orderNote;
    private final String orderTypeId;

    private OrderRequest(Builder b) {
        if (b.items == null || b.items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        if (b.customerName == null || b.customerName.isBlank()) {
            throw new IllegalArgumentException("customerName must not be blank");
        }
        this.items = Collections.unmodifiableList(new ArrayList<>(b.items));
        this.paymentType = b.paymentType != null ? b.paymentType : PaymentType.PAY_AT_RESTAURANT;
        this.customerName = b.customerName;
        this.customerPhone = b.customerPhone;
        this.orderNote = b.orderNote;
        this.orderTypeId = b.orderTypeId;
    }

    public List<OrderItem> getItems() { return items; }
    public PaymentType getPaymentType() { return paymentType; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getOrderNote() { return orderNote; }

    /**
     * Optional — if null, the library auto-selects the merchant's default order type.
     */
    public String getOrderTypeId() { return orderTypeId; }

    public long getTotalCents() {
        return items.stream().mapToLong(OrderItem::getSubtotal).sum();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final List<OrderItem> items = new ArrayList<>();
        private PaymentType paymentType = PaymentType.PAY_AT_RESTAURANT;
        private String customerName;
        private String customerPhone;
        private String orderNote;
        private String orderTypeId;

        public Builder addItem(OrderItem item) { this.items.add(item); return this; }
        public Builder items(List<OrderItem> items) { this.items.addAll(items); return this; }
        public Builder paymentType(PaymentType paymentType) { this.paymentType = paymentType; return this; }
        public Builder customerName(String customerName) { this.customerName = customerName; return this; }
        public Builder customerPhone(String customerPhone) { this.customerPhone = customerPhone; return this; }
        public Builder orderNote(String orderNote) { this.orderNote = orderNote; return this; }
        public Builder orderTypeId(String orderTypeId) { this.orderTypeId = orderTypeId; return this; }

        public OrderRequest build() { return new OrderRequest(this); }
    }
}
