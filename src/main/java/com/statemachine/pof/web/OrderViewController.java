package com.statemachine.pof.web;

import com.statemachine.pof.domain.Order;
import com.statemachine.pof.domain.OrderEvent;
import com.statemachine.pof.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrderViewController {

    private final OrderService orderService;

    public OrderViewController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String showOrderForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("eventForm", new EventForm());
        model.addAttribute("allEvents", getAvailableEvents());
        return "order-view";
    }

    @PostMapping("/create")
    public String createOrder(RedirectAttributes redirectAttributes) {
        Order newOrder = orderService.createOrder();
        redirectAttributes.addFlashAttribute("message", "Orden creada con ID: " + newOrder.getId());
        redirectAttributes.addFlashAttribute("newOrderId", newOrder.getId());
        return "redirect:/orders";
    }

    @PostMapping("/process-event")
    public String processEvent(@ModelAttribute("eventForm") EventForm eventForm, 
                             RedirectAttributes redirectAttributes) {
        try {
            Order updatedOrder = orderService.processEvent(eventForm.getOrderId(), eventForm.getEvent());
            redirectAttributes.addFlashAttribute("message", 
                "Evento procesado. Nuevo estado: " + updatedOrder.getState());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders";
    }

    private List<OrderEvent> getAvailableEvents() {
        return Arrays.asList(OrderEvent.values());
    }

    public static class EventForm {
        private UUID orderId;
        private OrderEvent event;

        // Getters y Setters
        public UUID getOrderId() {
            return orderId;
        }

        public void setOrderId(UUID orderId) {
            this.orderId = orderId;
        }

        public OrderEvent getEvent() {
            return event;
        }

        public void setEvent(OrderEvent event) {
            this.event = event;
        }
    }
}