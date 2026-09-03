package com.clover.sdk.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code clover.*} properties from application.properties / application.yml
 * to a typed object.
 *
 * <pre>
 * # application.properties
 * clover.merchant-id=ABC123XYZ
 * clover.token=your-api-token
 * clover.environment=SANDBOX        # SANDBOX (default) or PRODUCTION
 * </pre>
 *
 * <pre>
 * # application.yml
 * clover:
 *   merchant-id: ABC123XYZ
 *   token: your-api-token
 *   environment: SANDBOX
 * </pre>
 */
@ConfigurationProperties(prefix = "clover")
public class CloverProperties {

    /** Clover merchant ID (required). */
    private String merchantId;

    /** Clover API token (required). */
    private String token;

    /** Clover developer app ID — required for the Menus API. */
    private String appId;

    /** Clover PAKMS public key — required for Clover.js online payments. */
    private String pakmsKey;

    /**
     * Target environment. Accepts {@code SANDBOX} or {@code PRODUCTION}.
     * Defaults to {@code SANDBOX}.
     */
    private String environment = "SANDBOX";

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getPakmsKey() { return pakmsKey; }
    public void setPakmsKey(String pakmsKey) { this.pakmsKey = pakmsKey; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
}
