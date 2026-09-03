package com.bng.ordering.controller;

import com.bng.ordering.model.request.CreateOrderRequest;
import com.bng.ordering.model.response.ApiResponse;
import com.bng.ordering.model.response.OrderResponse;
import com.bng.ordering.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** POST /api/orders — create a new order */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest req) {
        OrderResponse order = orderService.createOrder(req);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Order placed successfully", order));
    }

    /** GET /api/orders/{id} — get order status */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String id) {
        return orderService.getOrder(id)
            .map(o -> ResponseEntity.ok(ApiResponse.ok(o)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Order not found: " + id)));
    }
}
