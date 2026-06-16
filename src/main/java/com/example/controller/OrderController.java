package com.example.controller;

import com.example.dto.OrderListResponse;
import com.example.service.OrderListService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderListService orderListService;

    public OrderController(OrderListService orderListService) {
        this.orderListService = orderListService;
    }

    @GetMapping
    public OrderListResponse list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return orderListService.listOrders(page, pageSize);
    }
}
