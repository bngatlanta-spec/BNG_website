package com.bng.ordering.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String chargeId;     // Clover Ecommerce charge ID (online payments only)
    private String status;       // PENDING, CONFIRMED, PREPARING, READY, COMPLETED
    private String customerName;
    private String customerPhone;
    private List<OrderLineItem> items;
    private double subtotal;
    private double tax;
    private double total;
    private String estimatedReadyIn; // e.g. "25-35 minutes"
    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLineItem {
        private String itemId;
        private String name;
        private int quantity;
        private double unitPrice;
        private double lineTotal;
        private String specialInstructions;
    }
}
