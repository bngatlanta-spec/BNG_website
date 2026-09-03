package com.clover.sdk;

/**
 * Configuration required to connect to the Clover API.
 * Use {@link #builder()} to construct an instance.
 *
 * <pre>
 * CloverConfig config = CloverConfig.builder()
 *     .merchantId("ABC123XYZ")
 *     .token("your-api-token")
 *     .environment(CloverEnvironment.SANDBOX)
 *     .build();
 * </pre>
 */
public class CloverConfig {

    private final String merchantId;
    private final String token;
    private final String appId;
    private final String pakmsKey;
    private final CloverEnvironment environment;

    private CloverConfig(Builder builder) {
        if (builder.merchantId == null || builder.merchantId.isBlank()) {
            throw new IllegalArgumentException("merchantId must not be blank");
        }
        if (builder.token == null || builder.token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        this.merchantId = builder.merchantId;
        this.token = builder.token;
        this.appId = builder.appId;
        this.pakmsKey = builder.pakmsKey;
        this.environment = builder.environment != null ? builder.environment : CloverEnvironment.SANDBOX;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getToken() {
        return token;
    }

    public String getAppId() {
        return appId;
    }

    public String getPakmsKey() {
        return pakmsKey;
    }

    public String getEcommerceBaseUrl() {
        return environment.getEcommerceBaseUrl();
    }

    public String getJsSdkUrl() {
        return environment.getJsSdkUrl();
    }

    public CloverEnvironment getEnvironment() {
        return environment;
    }

    public String getBaseUrl() {
        return environment.getBaseUrl();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String merchantId;
        private String token;
        private String appId;
        private String pakmsKey;
        private CloverEnvironment environment = CloverEnvironment.SANDBOX;

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder pakmsKey(String pakmsKey) {
            this.pakmsKey = pakmsKey;
            return this;
        }

        public Builder environment(CloverEnvironment environment) {
            this.environment = environment;
            return this;
        }

        public CloverConfig build() {
            return new CloverConfig(this);
        }
    }
}
