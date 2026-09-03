package com.bng.ordering.model.request;

import lombok.Data;

@Data
public class OrderModifierRequest {
    private String modifierId;
    private String name;
    private long price; // in cents
}
