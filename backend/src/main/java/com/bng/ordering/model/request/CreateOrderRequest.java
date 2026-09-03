package com.bng.ordering.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer phone is required")
    private String customerPhone;

    private String customerEmail;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;

    private String orderType; // DINE_IN, TAKEOUT

    private String notes;

    /**
     * Controls how the order is recorded in Clover.
     * <ul>
     *   <li>{@code PAY_AT_RESTAURANT} (default) — order is open; customer pays at the counter.</li>
     *   <li>{@code PREPAID} — order is marked as paid immediately (online payment already collected).</li>
     * </ul>
     */
    private String paymentType = "PAY_AT_RESTAURANT";

    /** Clover.js card token — required when paymentType is PREPAID (Pay Online). */
    private String cardToken;
}
