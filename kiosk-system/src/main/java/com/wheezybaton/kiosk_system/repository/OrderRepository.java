package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Order;
import com.wheezybaton.kiosk_system.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfDay")
    Long countOrdersSince(LocalDateTime startOfDay);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);
}