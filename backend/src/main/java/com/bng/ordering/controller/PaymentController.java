package com.bng.ordering.controller;

import com.bng.ordering.model.request.PaymentRequest;
import com.bng.ordering.model.response.ApiResponse;
import com.bng.ordering.model.response.PaymentResponse;
import com.bng.ordering.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** POST /api/payments — process payment for an order */
    @PostMapping
    public ApiResponse<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest req) {
        return ApiResponse.ok("Payment processed", paymentService.processPayment(req));
    }
}
