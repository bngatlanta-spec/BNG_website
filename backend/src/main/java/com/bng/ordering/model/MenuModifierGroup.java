package com.bng.ordering.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuModifierGroup {
    private String id;
    private String name;
    private int minRequired; // 0 = optional, 1+ = required
    private int maxAllowed;  // 1 = single-select, 2+ = multi-select
    private List<MenuModifier> modifiers;
}
