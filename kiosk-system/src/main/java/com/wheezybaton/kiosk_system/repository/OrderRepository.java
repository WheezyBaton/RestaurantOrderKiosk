package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Order;
import com.wheezybaton.kiosk_system.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);
}