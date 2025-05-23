package com.statemachine.pof.api;

import com.statemachine.pof.domain.Order;
import com.statemachine.pof.domain.OrderEvent;
import com.statemachine.pof.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder() {
        Order order = orderService.createOrder();
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/event")
    public ResponseEntity<Order> processOrderEvent(
            @PathVariable UUID id,
            @RequestParam OrderEvent event) {
        
        Order order = orderService.processEvent(id, event);
        return ResponseEntity.ok(order);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleOrderNotFound(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}