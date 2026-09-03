package com.bng.ordering.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuModifier {
    private String id;
    private String name;
    private double price; // in dollars (0 = free)
}
