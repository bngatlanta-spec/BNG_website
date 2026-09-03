package com.bng.ordering.service;

import com.bng.ordering.model.request.PaymentRequest;
import com.bng.ordering.model.response.PaymentResponse;
import com.clover.sdk.CloverClient;
import com.clover.sdk.exception.CloverException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PaymentService {

    private final CloverClient cloverClient;

    public PaymentService(CloverClient cloverClient) {
        this.cloverClient = cloverClient;
    }

    /**
     * Charges a card via Clover Ecommerce using a Clover.js token.
     * @param amountCents total including tax, in cents
     * @param cardToken   token from Clover.js
     * @return charge ID from Clover
     */
    public String chargeCard(long amountCents, String cardToken) {
        try {
            JsonNode result = cloverClient.chargeCard(amountCents, cardToken);
            String status = result.path("status").asText("");
            if (!"succeeded".equalsIgnoreCase(status)) {
                String failureMessage = result.path("outcome").path("seller_message").asText("Card declined");
                throw new RuntimeException("Payment declined: " + failureMessage);
            }
            return result.path("id").asText("CHARGE-UNKNOWN");
        } catch (CloverException e) {
            throw new RuntimeException("Payment failed: " + e.getMessage(), e);
        }
    }

    /** Legacy endpoint — kept for backward compatibility. */
    public PaymentResponse processPayment(PaymentRequest req) {
        String chargeId = chargeCard(
            (long)(req.getAmount() * 100),
            req.getCardToken()
        );
        return PaymentResponse.builder()
            .paymentId(chargeId)
            .orderId(req.getOrderId())
            .status("APPROVED")
            .amount(req.getAmount())
            .paymentMethod(req.getPaymentMethod())
            .last4("****")
            .processedAt(Instant.now())
            .build();
    }
}
