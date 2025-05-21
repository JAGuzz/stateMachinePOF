package com.statemachine.pof.api;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
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
		//recupera la orden de la BD
		Optional<Order> orderOpt = orderRepository.findById(id);
		if (orderOpt.isEmpty()) return "Order not found";
		
		//recupera la maquina de estados (state machine)
		StateMachine<OrderState, OrderEvent> sm = factory.getStateMachine(id.toString());
		
		//inicia la maquina de estados
		sm.startReactively().block();
		
		//recupera el estado a partir del valor en la BD para la persistencia de los estados
		sm.getStateMachineAccessor().doWithAllRegions(access -> access.resetStateMachineReactively(
				new DefaultStateMachineContext<>(orderOpt.get().getState(), null, null, null, null, id.toString()))
				.block());

//        sm.sendEvent(Mono.just(
//            MessageBuilder.withPayload(event).build())).block();
		
		//envia el evento a la maquina de estados
		Mono<Message<OrderEvent>> messageMono = Mono.just(MessageBuilder.withPayload(event).build());
		sm.sendEvent(messageMono).blockLast();
		
		//actualiza el estado en la BD
		Order order = orderOpt.get();
		order.setState(sm.getState().getId());
		orderRepository.save(order);

		return "Order state: " + order.getState().toString() + " Id: " + order.getId();
	}

//    @GetMapping("/fetch")
//    public List<StateMachine<OrderState, OrderEvent>> fetchAll() {
//    	
//    }
}
