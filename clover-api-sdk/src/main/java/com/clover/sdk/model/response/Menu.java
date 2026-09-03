package com.clover.sdk.model.response;

import com.clover.sdk.internal.CloverList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Menu {
    private String id;
    private String name;
    private Boolean enabled;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    @Override
    public String toString() {
        return "Menu{id='" + id + "', name='" + name + "', enabled=" + enabled + "}";
    }
}
