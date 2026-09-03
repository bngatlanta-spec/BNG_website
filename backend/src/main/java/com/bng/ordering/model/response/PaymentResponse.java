package com.bng.ordering.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private String status;       // APPROVED, DECLINED, PENDING
    private double amount;
    private String paymentMethod;
    private String last4;        // last 4 digits of card (from Clover token in prod)
    private Instant processedAt;
}
