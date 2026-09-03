package com.clover.sdk.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrintEvent {
    private String id;
    private String state;

    // Non-null when the print event call failed (non-fatal — order still exists)
    private String error;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public boolean isSuccess() { return error == null; }

    @Override
    public String toString() {
        return error != null
                ? "PrintEvent{error='" + error + "'}"
                : "PrintEvent{id='" + id + "', state='" + state + "'}";
    }
}
