package com.clover.sdk.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderType {
    private String id;
    private String label;
    private String labelKey;
    private Boolean isHidden;
    private Boolean isDefault;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getLabelKey() { return labelKey; }
    public void setLabelKey(String labelKey) { this.labelKey = labelKey; }

    public Boolean getIsHidden() { return isHidden; }
    public void setIsHidden(Boolean isHidden) { this.isHidden = isHidden; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public String getDisplayLabel() {
        return label != null ? label : (labelKey != null ? labelKey : id);
    }

    @Override
    public String toString() {
        return "OrderType{id='" + id + "', label='" + getDisplayLabel() + "', isDefault=" + isDefault + "}";
    }
}
