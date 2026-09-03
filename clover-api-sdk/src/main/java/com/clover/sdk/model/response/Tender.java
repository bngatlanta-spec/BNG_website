package com.clover.sdk.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tender {
    private String id;
    private String label;
    private Boolean enabled;
    private Boolean visible;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Boolean getVisible() { return visible; }
    public void setVisible(Boolean visible) { this.visible = visible; }

    public boolean isCash() {
        return label != null && label.toLowerCase().contains("cash");
    }

    @Override
    public String toString() {
        return "Tender{id='" + id + "', label='" + label + "'}";
    }
}
