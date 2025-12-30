package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Order;
import com.wheezybaton.kiosk_system.model.OrderStatus;
import com.wheezybaton.kiosk_system.model.OrderType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private TestEntityManager entityManager;

    private Order createOrder(int number, LocalDateTime date, OrderStatus status) {
        return new Order(null, number, date, status, OrderType.TAKE_AWAY, BigDecimal.TEN, null);
    }

    @Test
    void shouldFindOrdersByStatus() {
        orderRepo.deleteAll();

        entityManager.persist(createOrder(1, LocalDateTime.now(), OrderStatus.READY));
        entityManager.persist(createOrder(2, LocalDateTime.now(), OrderStatus.NEW));
        entityManager.persist(createOrder(3, LocalDateTime.now(), OrderStatus.READY));
        entityManager.flush();

        List<Order> readyOrders = orderRepo.findByStatusInOrderByCreatedAtAsc(List.of(OrderStatus.READY));

        assertThat(readyOrders).hasSize(2);
        assertThat(readyOrders).allMatch(o -> o.getStatus() == OrderStatus.READY);
    }

    @Test
    void shouldSaveAndRetrieveOrder() {
        Order order = new Order(null, 100, LocalDateTime.now(), OrderStatus.NEW, OrderType.EAT_IN, BigDecimal.valueOf(50.00), null);

        Order saved = orderRepo.save(order);
        Order found = orderRepo.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getDailyNumber()).isEqualTo(100);
        assertThat(found.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }
}