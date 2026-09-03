package com.clover.sdk.model.response;

import com.clover.sdk.internal.CloverList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
    private String id;
    private String name;
    private Integer sortOrder;

    // Clover returns items as {"elements": [...]}
    private CloverList<MenuItemRef> items;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public CloverList<MenuItemRef> getItems() { return items; }
    public void setItems(CloverList<MenuItemRef> items) { this.items = items; }

    /** Convenience method — unwraps the nested elements list. */
    public List<MenuItemRef> getItemList() {
        return items != null ? items.getElements() : Collections.emptyList();
    }

    @Override
    public String toString() {
        return "Category{id='" + id + "', name='" + name + "', items=" + getItemList().size() + "}";
    }
}
