package com.clover.sdk.model.response;

import com.clover.sdk.internal.CloverList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModifierGroup {
    private String id;
    private String name;
    private Integer minRequired;
    private Integer maxAllowed;
    private CloverList<Modifier> modifiers;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getMinRequired() { return minRequired; }
    public void setMinRequired(Integer minRequired) { this.minRequired = minRequired; }

    public Integer getMaxAllowed() { return maxAllowed; }
    public void setMaxAllowed(Integer maxAllowed) { this.maxAllowed = maxAllowed; }

    public CloverList<Modifier> getModifiers() { return modifiers; }
    public void setModifiers(CloverList<Modifier> modifiers) { this.modifiers = modifiers; }

    public List<Modifier> getModifierList() {
        return modifiers != null ? modifiers.getElements() : Collections.emptyList();
    }
}
