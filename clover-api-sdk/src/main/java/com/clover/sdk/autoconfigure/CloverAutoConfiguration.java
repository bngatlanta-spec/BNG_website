package com.clover.sdk.autoconfigure;

import com.clover.sdk.CloverClient;
import com.clover.sdk.CloverConfig;
import com.clover.sdk.CloverEnvironment;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the Clover API SDK.
 *
 * <p>Activated automatically when {@code clover.merchant-id} and {@code clover.token}
 * are present in the application properties. Registers a {@link CloverClient} bean
 * that can be injected anywhere in the application.
 *
 * <p>Override either bean in your own {@code @Configuration} class to customise the setup.
 */
@AutoConfiguration
@ConditionalOnClass(CloverClient.class)
@ConditionalOnProperty(prefix = "clover", name = {"merchant-id", "token"})
@EnableConfigurationProperties(CloverProperties.class)
public class CloverAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CloverConfig cloverConfig(CloverProperties properties) {
        CloverEnvironment env;
        try {
            env = CloverEnvironment.valueOf(properties.getEnvironment().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid clover.environment value: '" + properties.getEnvironment()
                    + "'. Must be SANDBOX or PRODUCTION.");
        }

        return CloverConfig.builder()
                .merchantId(properties.getMerchantId())
                .token(properties.getToken())
                .appId(properties.getAppId())
                .pakmsKey(properties.getPakmsKey())
                .environment(env)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CloverClient cloverClient(CloverConfig cloverConfig) {
        return new CloverClient(cloverConfig);
    }
}
