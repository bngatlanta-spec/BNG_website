package com.bng.ordering.controller;

import com.bng.ordering.model.response.ApiResponse;
import com.clover.sdk.CloverClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final CloverClient cloverClient;

    public ConfigController(CloverClient cloverClient) {
        this.cloverClient = cloverClient;
    }

    /**
     * GET /api/config/clover-key
     * Returns the public PAKMS key and Clover.js SDK URL for the frontend.
     * The PAKMS key is safe to expose — it is a public key used only to tokenize cards.
     */
    @GetMapping("/clover-key")
    public ApiResponse<Map<String, String>> getCloverKey() {
        return ApiResponse.ok(Map.of(
            "pakmsKey", String.valueOf(cloverClient.getConfig().getPakmsKey()),
            "sdkUrl",   cloverClient.getConfig().getJsSdkUrl()
        ));
    }
}
