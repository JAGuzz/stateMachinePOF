package com.statemachine.pof.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import com.statemachine.pof.domain.Order;
import com.statemachine.pof.domain.OrderEvent;
import com.statemachine.pof.domain.OrderState;
import com.statemachine.pof.persistance.OrderRepository;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {
	
	@Autowired
	private OrderRepository orderRepository;

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states) throws Exception {
        states
            .withStates()
            .initial(OrderState.NEW)
            .state(OrderState.VALIDATED)
            .state(OrderState.SHIPPED)
            .state(OrderState.DELIVERED)
            .state(OrderState.CANCELLED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
        transitions
            .withExternal().source(OrderState.NEW).target(OrderState.VALIDATED).event(OrderEvent.VALIDATE_ORDER)
			.action(validateOrderAction()).action(persistOrderStateAction())
            .and()
            .withExternal().source(OrderState.VALIDATED).target(OrderState.SHIPPED).event(OrderEvent.SHIP_ORDER)
			.action(shipOrderAction()).action(persistOrderStateAction())
            .and()
            .withExternal().source(OrderState.SHIPPED).target(OrderState.DELIVERED).event(OrderEvent.DELIVER_ORDER)
			.action(confirmDeliveryAction()).action(persistOrderStateAction())
            .and()
            .withExternal().source(OrderState.NEW).target(OrderState.CANCELLED).event(OrderEvent.CANCEL_ORDER)
			.action(cancelOrderAction()).action(persistOrderStateAction())
            .and()
            .withExternal().source(OrderState.VALIDATED).target(OrderState.CANCELLED).event(OrderEvent.CANCEL_ORDER)
			.action(cancelOrderAction()).action(persistOrderStateAction())
            .and()
            .withExternal().source(OrderState.SHIPPED).target(OrderState.CANCELLED).event(OrderEvent.CANCEL_ORDER)
			.action(cancelOrderAction()).action(persistOrderStateAction());
    }
    
    @Bean
    public Action<OrderState, OrderEvent> validateOrderAction() {
        return context -> {
            Order order = (Order) context.getMessageHeader("order");
            System.out.println("[VALIDATE] Validating order with ID: " + order.getId());

            // Simulación de verificación de inventario o datos del cliente
            System.out.println("[VALIDATE] Calling InventoryService to check stock...");
            System.out.println("[VALIDATE] Calling CustomerService to verify customer info...");
        };
    }

    @Bean
    public Action<OrderState, OrderEvent> shipOrderAction() {
        return context -> {
            Order order = (Order) context.getMessageHeader("order");
            System.out.println("[SHIP] Shipping order with ID: " + order.getId());

            // Simulación de llamada a servicio de logística
            System.out.println("[SHIP] Calling LogisticsService to generate shipping label...");
            System.out.println("[SHIP] Tracking code generated: TRK" + order.getId().toString().substring(0, 8));
        };
    }

    @Bean
    public Action<OrderState, OrderEvent> confirmDeliveryAction() {
        return context -> {
            Order order = (Order) context.getMessageHeader("order");
            System.out.println("[DELIVER] Confirming delivery for order ID: " + order.getId());

            // Simulación de verificación de entrega
            System.out.println("[DELIVER] Contacting CourierService to confirm delivery...");
            System.out.println("[DELIVER] Delivery confirmed.");
        };
    }

    @Bean
    public Action<OrderState, OrderEvent> cancelOrderAction() {
        return context -> {
            Order order = (Order) context.getMessageHeader("order");
            System.out.println("[CANCEL] Cancelling order with ID: " + order.getId());

            // Simulación de reembolso y liberación de recursos
            System.out.println("[CANCEL] Calling PaymentService to process refund...");
            System.out.println("[CANCEL] Calling InventoryService to release reserved items...");
            System.out.println("[CANCEL] Sending cancellation email to customer...");
        };
    }

    @Bean
    public Action<OrderState, OrderEvent> persistOrderStateAction() {
        return context -> {
            Order order = (Order) context.getMessageHeader("order");
            if (order != null) {
                OrderState newState = context.getTarget().getId();
                order.setState(newState);
                orderRepository.save(order);
                System.out.println("[PERSIST] Order " + order.getId() + " persisted with state: " + newState);
            }
        };
    }


    
}
// Esta clase se encarga de configurar los estados que seran vaidos para la stateMachine ademas de tambien definir 
// las transisiones entre los posibles estados