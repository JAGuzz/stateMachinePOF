package api;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.statemachine.pof.domain.Order;
import com.statemachine.pof.domain.OrderEvent;
import com.statemachine.pof.domain.OrderState;
import com.statemachine.pof.persistance.OrderRepository;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StateMachineFactory<OrderState, OrderEvent> factory;

    @PostMapping
    public Order create() {
        Order order = new Order(OrderState.NEW);
        return orderRepository.save(order);
    }

    @PostMapping("/{id}/event")
    public String processEvent(@PathVariable UUID id, @RequestParam OrderEvent event) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) return "Order not found";

        StateMachine<OrderState, OrderEvent> sm = factory.getStateMachine(id.toString());
        sm.start();
        sm.sendEvent(event);

        Order order = orderOpt.get();
        order.setState(sm.getState().getId());
        orderRepository.save(order);

        return "Order state: " + order.getState().toString();
    }
}
