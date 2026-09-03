package com.clover.sdk.model.response;

import com.clover.sdk.PaymentType;

/**
 * Result returned by {@code CloverClient.placeOrder()}.
 * Always check {@link #isSuccess()} before using other fields.
 */
public class OrderResult {

    private final boolean success;
    private final String orderId;
    private final String orderState;
    private final PaymentType paymentType;
    private final Order order;
    private final Payment payment;
    private final PrintEvent printEvent;
    private final Customer customer;
    private final String paymentFailedReason;

    private OrderResult(Builder b) {
        this.success = b.success;
        this.orderId = b.orderId;
        this.orderState = b.orderState;
        this.paymentType = b.paymentType;
        this.order = b.order;
        this.payment = b.payment;
        this.printEvent = b.printEvent;
        this.customer = b.customer;
        this.paymentFailedReason = b.paymentFailedReason;
    }

    public boolean isSuccess() { return success; }

    public String getOrderId() { return orderId; }

    /** Human-readable description of the order state (e.g. "paid", "open (pay at restaurant)"). */
    public String getOrderState() { return orderState; }

    public PaymentType getPaymentType() { return paymentType; }

    /** The full order object fetched after creation (includes line items and customers). */
    public Order getOrder() { return order; }

    /** Non-null only when {@code paymentType == PREPAID} and the payment was recorded successfully. */
    public Payment getPayment() { return payment; }

    /** Non-null after an order placement attempt. Check {@link PrintEvent#isSuccess()} for status. */
    public PrintEvent getPrintEvent() { return printEvent; }

    /** The Clover customer record that was created and linked to the order. */
    public Customer getCustomer() { return customer; }

    /**
     * Non-null when {@code paymentType == PREPAID} but the payment recording failed.
     * The order still exists in Clover; it just isn't marked as paid.
     */
    public String getPaymentFailedReason() { return paymentFailedReason; }

    public boolean isPrepaidSuccessful() {
        return paymentType == PaymentType.PREPAID && payment != null && paymentFailedReason == null;
    }

    @Override
    public String toString() {
        return "OrderResult{orderId='" + orderId + "', state='" + orderState + "', paymentType=" + paymentType + "}";
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean success;
        private String orderId;
        private String orderState;
        private PaymentType paymentType;
        private Order order;
        private Payment payment;
        private PrintEvent printEvent;
        private Customer customer;
        private String paymentFailedReason;

        public Builder success(boolean success) { this.success = success; return this; }
        public Builder orderId(String orderId) { this.orderId = orderId; return this; }
        public Builder orderState(String orderState) { this.orderState = orderState; return this; }
        public Builder paymentType(PaymentType paymentType) { this.paymentType = paymentType; return this; }
        public Builder order(Order order) { this.order = order; return this; }
        public Builder payment(Payment payment) { this.payment = payment; return this; }
        public Builder printEvent(PrintEvent printEvent) { this.printEvent = printEvent; return this; }
        public Builder customer(Customer customer) { this.customer = customer; return this; }
        public Builder paymentFailedReason(String reason) { this.paymentFailedReason = reason; return this; }

        public OrderResult build() { return new OrderResult(this); }
    }
}
