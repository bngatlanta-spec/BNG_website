package com.clover.sdk.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Modifier {
    private String id;
    private String name;
    private Long price; // price in cents (0 = free)

    public String getId()   { return id; }
    public void setId(String id) { this.id = id; }

    public String getName()     { return name; }
    public void setName(String name) { this.name = name; }

    public Long getPrice()      { return price; }
    public void setPrice(Long price) { this.price = price; }

    public double getPriceDollars() {
        return price != null ? price / 100.0 : 0.0;
    }
}
