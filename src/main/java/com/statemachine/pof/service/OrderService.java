package com.statemachine.pof.service;

import com.statemachine.pof.domain.Order;
import com.statemachine.pof.domain.OrderEvent;
import com.statemachine.pof.domain.OrderState;
import com.statemachine.pof.persistance.OrderRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;

    public OrderService(OrderRepository orderRepository, 
                      StateMachineFactory<OrderState, OrderEvent> stateMachineFactory) {
        this.orderRepository = orderRepository;
        this.stateMachineFactory = stateMachineFactory;
    }

    public Order createOrder() {
        Order order = new Order(OrderState.NEW);
        return orderRepository.save(order);
    }

    public Order processEvent(UUID orderId, OrderEvent event) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found with id: " + orderId);
        }

        Order order = orderOpt.get();
        StateMachine<OrderState, OrderEvent> sm = processStateMachineEvent(order, event);
        
        // Update order state from the state machine
        order.setState(sm.getState().getId());
        return orderRepository.save(order);
    }

    private StateMachine<OrderState, OrderEvent> processStateMachineEvent(Order order, OrderEvent event) {
        StateMachine<OrderState, OrderEvent> sm = stateMachineFactory.getStateMachine(order.getId().toString());
        
        sm.startReactively().block();
        
        sm.getStateMachineAccessor().doWithAllRegions(access -> 
            access.resetStateMachineReactively(
                new DefaultStateMachineContext<>(
                    order.getState(), 
                    null, 
                    null, 
                    null, 
                    null, 
                    order.getId().toString()
                )
            ).block()
        );
        
        Message<OrderEvent> message = MessageBuilder.withPayload(event)
                .setHeader("order", order)
                .build();
                
        sm.sendEvent(Mono.just(message)).blockLast();
        
        return sm;
    }
}