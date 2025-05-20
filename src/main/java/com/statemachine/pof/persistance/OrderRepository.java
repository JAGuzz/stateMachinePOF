package com.statemachine.pof.persistance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.statemachine.pof.domain.Order;

public interface OrderRepository extends JpaRepository<Order, UUID> {

}
