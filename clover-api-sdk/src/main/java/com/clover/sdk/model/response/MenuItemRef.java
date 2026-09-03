package com.clover.sdk.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Lightweight item reference as returned inside a category's item list. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuItemRef {
    private String id;
    private String name;
    private Long price;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }
}
