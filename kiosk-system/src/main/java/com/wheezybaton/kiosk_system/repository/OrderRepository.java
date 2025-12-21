package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfDay")
    Long countOrdersSince(LocalDateTime startOfDay);
}