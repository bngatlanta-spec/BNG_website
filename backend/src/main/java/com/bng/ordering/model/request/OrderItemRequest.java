package com.bng.ordering.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class OrderItemRequest {

    @NotBlank(message = "Item ID is required")
    private String itemId;

    @NotBlank(message = "Item name is required")
    private String name;

    /** Base item price in cents (e.g. 1899 = $18.99). */
    @Positive(message = "Item price must be positive")
    private long price;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private String specialInstructions;

    /** Selected modifiers (extras) for this item. */
    private List<OrderModifierRequest> modifiers;
}
