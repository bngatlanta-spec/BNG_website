package com.clover.sdk.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

/**
 * Generic wrapper for Clover's paginated response envelope: {@code {"elements": [...]}}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloverList<T> {

    private List<T> elements;

    public List<T> getElements() {
        return elements != null ? elements : Collections.emptyList();
    }

    public void setElements(List<T> elements) {
        this.elements = elements;
    }
}
