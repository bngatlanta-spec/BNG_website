package com.clover.sdk;

public enum CloverEnvironment {
    SANDBOX(
        "https://sandbox.dev.clover.com",
        "https://scl-sandbox.dev.clover.com",
        "https://checkout.sandbox.dev.clover.com/sdk.js"
    ),
    PRODUCTION(
        "https://api.clover.com",
        "https://scl.clover.com",
        "https://checkout.clover.com/sdk.js"
    );

    private final String baseUrl;
    private final String ecommerceBaseUrl;
    private final String jsSdkUrl;

    CloverEnvironment(String baseUrl, String ecommerceBaseUrl, String jsSdkUrl) {
        this.baseUrl = baseUrl;
        this.ecommerceBaseUrl = ecommerceBaseUrl;
        this.jsSdkUrl = jsSdkUrl;
    }

    public String getBaseUrl()          { return baseUrl; }
    public String getEcommerceBaseUrl() { return ecommerceBaseUrl; }
    public String getJsSdkUrl()         { return jsSdkUrl; }
}
