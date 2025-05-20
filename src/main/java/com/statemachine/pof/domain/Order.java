package com.statemachine.pof.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "orders_table")
@NoArgsConstructor
@Getter
@Setter
public class Order {
	
    @Id
    private UUID id = UUID.randomUUID();
    
    @Enumerated(EnumType.STRING)
    private OrderState state;

    public Order() {}

    public Order(OrderState state) {
        this.state = state;
    }

	public void setState(OrderState state) {
		this.state = state;
		
	}

	public OrderState getState() {
		
		return this.state;
	}

	public UUID getId() {
		return id;
	}

	
}
