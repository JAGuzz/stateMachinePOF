package com.statemachine.pof.statemachine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import com.statemachine.pof.domain.OrderEvent;
import com.statemachine.pof.domain.OrderState;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {

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
            .and()
            .withExternal().source(OrderState.VALIDATED).target(OrderState.SHIPPED).event(OrderEvent.SHIP_ORDER)
            .and()
            .withExternal().source(OrderState.SHIPPED).target(OrderState.DELIVERED).event(OrderEvent.DELIVER_ORDER)
            .and()
            .withExternal().source(OrderState.NEW).target(OrderState.CANCELLED).event(OrderEvent.CANCEL_ORDER)
            .and()
            .withExternal().source(OrderState.VALIDATED).target(OrderState.CANCELLED).event(OrderEvent.CANCEL_ORDER)
            .and()
            .withExternal().source(OrderState.SHIPPED).target(OrderState.CANCELLED).event(OrderEvent.CANCEL_ORDER);
    }
}
// Esta clase se encarga de configurar los estados que seran validos para la stateMachine ademas de tambien definir 
// las transisiones entre los posibles estados