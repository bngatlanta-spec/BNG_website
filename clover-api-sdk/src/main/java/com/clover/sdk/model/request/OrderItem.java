package com.clover.sdk.model.request;

/**
 * Represents a single item (with quantity) to include in an order.
 */
public class OrderItem {

    private final String id;
    private final String name;
    private final long price;
    private final int quantity;

    private OrderItem(Builder b) {
        if (b.id == null || b.id.isBlank()) throw new IllegalArgumentException("item id must not be blank");
        if (b.name == null || b.name.isBlank()) throw new IllegalArgumentException("item name must not be blank");
        if (b.price < 0) throw new IllegalArgumentException("item price must not be negative");
        if (b.quantity < 1) throw new IllegalArgumentException("item quantity must be at least 1");
        this.id = b.id;
        this.name = b.name;
        this.price = b.price;
        this.quantity = b.quantity;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    /** Price in cents (e.g., 1250 = $12.50). */
    public long getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public long getSubtotal() { return price * quantity; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String name;
        private long price;
        private int quantity = 1;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        /** Price in cents. */
        public Builder price(long price) { this.price = price; return this; }
        public Builder quantity(int quantity) { this.quantity = quantity; return this; }

        public OrderItem build() { return new OrderItem(this); }
    }

    @Override
    public String toString() {
        return "OrderItem{id='" + id + "', name='" + name + "', qty=" + quantity + "}";
    }
}
