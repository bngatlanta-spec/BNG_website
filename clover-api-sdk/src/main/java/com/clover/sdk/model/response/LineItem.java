package com.clover.sdk.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LineItem {
    private String id;
    private String name;
    private Long price;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public String getPriceFormatted() {
        if (price == null) return "$0.00";
        return String.format("$%.2f", price / 100.0);
    }

    @Override
    public String toString() {
        return "LineItem{name='" + name + "', price=" + getPriceFormatted() + "}";
    }
}
