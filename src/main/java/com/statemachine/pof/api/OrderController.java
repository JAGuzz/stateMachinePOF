package com.statemachine.pof.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.statemachine.pof.domain.Order;
import com.statemachine.pof.domain.OrderEvent;
import com.statemachine.pof.domain.OrderState;
import com.statemachine.pof.persistance.OrderRepository;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StateMachineFactory<OrderState, OrderEvent> factory;

    @PostMapping("")
    public Order create() {
        Order order = new Order(OrderState.NEW);
        return orderRepository.save(order);
    }

    @PostMapping("/{id}/event")
    public String processEvent(@PathVariable UUID id, @RequestParam OrderEvent event) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) return "Order not found";

        StateMachine<OrderState, OrderEvent> sm = factory.getStateMachine(id.toString());
        
        sm.startReactively().block();

//        sm.sendEvent(Mono.just(
//            MessageBuilder.withPayload(event).build())).block();
        Mono<Message<OrderEvent>> messageMono = Mono.just(MessageBuilder.withPayload(event).build());
        sm.sendEvent(messageMono).blockLast();


        Order order = orderOpt.get();
        order.setState(sm.getState().getId());
        orderRepository.save(order);

        return "Order state: " + order.getState().toString() + "Id: " + order.getId();
    }
    
//    @GetMapping("/fetch")
//    public List<StateMachine<OrderState, OrderEvent>> fetchAll() {
//    	
//    }
}
