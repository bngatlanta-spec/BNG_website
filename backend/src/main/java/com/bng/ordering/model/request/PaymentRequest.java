package com.bng.ordering.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotBlank(message = "Order ID is required")
    private String orderId;

    @Positive(message = "Amount must be positive")
    private double amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CARD, CASH, CLOVER_ONLINE

    // Card details (only needed for CARD — will be tokenised by Clover in production)
    private String cardToken;
}
