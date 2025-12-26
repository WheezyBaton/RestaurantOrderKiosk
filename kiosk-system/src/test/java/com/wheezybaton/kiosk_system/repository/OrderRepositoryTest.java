package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Order;
import com.wheezybaton.kiosk_system.model.OrderStatus;
import com.wheezybaton.kiosk_system.model.OrderType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

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

    @Test
    @Transactional
    void shouldCountOrdersSinceSpecificDate() {
        orderRepo.deleteAll();

        Order oldOrder = createOrder(1, LocalDateTime.now().minusDays(2), OrderStatus.COMPLETED);
        Order todayOrder1 = createOrder(2, LocalDateTime.now(), OrderStatus.NEW);
        Order todayOrder2 = createOrder(3, LocalDateTime.now().plusHours(1), OrderStatus.IN_PROGRESS);

        oldOrder = orderRepo.save(oldOrder);
        orderRepo.save(todayOrder1);
        orderRepo.save(todayOrder2);

        entityManager.flush();

        entityManager.getEntityManager()
                .createNativeQuery("UPDATE orders SET created_at = :date WHERE id = :id")
                .setParameter("date", LocalDateTime.now().minusDays(2))
                .setParameter("id", oldOrder.getId())
                .executeUpdate();

        entityManager.clear();

        LocalDateTime startOfDay = LocalDateTime.now().minusHours(12);
        Long count = orderRepo.countOrdersSince(startOfDay);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindOrdersByStatus() {
        orderRepo.deleteAll();

        entityManager.persist(createOrder(1, LocalDateTime.now(), OrderStatus.READY));
        entityManager.persist(createOrder(2, LocalDateTime.now(), OrderStatus.NEW));
        entityManager.persist(createOrder(3, LocalDateTime.now(), OrderStatus.READY));
        entityManager.flush();

        List<Order> readyOrders = orderRepo.findByStatus(OrderStatus.READY);

        assertThat(readyOrders).hasSize(2);
        assertThat(readyOrders).allMatch(o -> o.getStatus() == OrderStatus.READY);
    }

    @Test
    void shouldSaveAndRetrieveOrder() {
        Order order = new Order();
        order.setDailyNumber(100);
        order.setStatus(OrderStatus.NEW);
        order.setType(OrderType.EAT_IN);
        order.setTotalAmount(BigDecimal.valueOf(50.00));
        order.setCreatedAt(LocalDateTime.now());

        Order saved = orderRepo.save(order);
        Order found = orderRepo.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getDailyNumber()).isEqualTo(100);
        assertThat(found.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
    }

    private Order createOrder(int number, LocalDateTime date, OrderStatus status) {
        Order o = new Order();
        o.setDailyNumber(number);
        o.setCreatedAt(date);
        o.setStatus(status);
        o.setType(OrderType.TAKE_AWAY);
        o.setTotalAmount(BigDecimal.TEN);
        return o;
    }
}