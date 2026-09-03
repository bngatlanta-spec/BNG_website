package com.bng.ordering.model.response;

import com.bng.ordering.model.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {
    private List<MenuCategory> categories;
    private int totalItems;
}
