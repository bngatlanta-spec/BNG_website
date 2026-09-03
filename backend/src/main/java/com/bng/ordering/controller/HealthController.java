package com.bng.ordering.controller;

import com.bng.ordering.model.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(Map.of(
            "status", "UP",
            "service", "BNG Ordering API",
            "timestamp", Instant.now().toString(),
            "mode", "dummy"   // change to "live" when Clover is wired
        ));
    }
}
