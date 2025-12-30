package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.SalesStatDto;
import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.OrderRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class StatsServiceIntegrationTest {

    @Autowired
    private StatsService statsService;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Test
    void shouldCalculateStats_OnlyForCompletedOrders() {
        Product burger = productRepo.save(new Product(null, "Test Burger", BigDecimal.valueOf(20), "", "", true, null, null, false));

        createTestOrder(burger, OrderStatus.COMPLETED, 2);
        createTestOrder(burger, OrderStatus.NEW, 5);

        List<SalesStatDto> stats = statsService.getSalesStats();

        assertThat(stats).isNotEmpty();

        SalesStatDto stat = stats.stream()
                .filter(s -> s.getProductName().equals("Test Burger"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Statystyki dla 'Test Burger' nie zostały znalezione"));

        assertThat(stat.getTotalQuantity()).isEqualTo(2);
        assertThat(stat.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(40.00));
    }

    private void createTestOrder(Product product, OrderStatus status, int quantity) {
        BigDecimal lineTotal = product.getBasePrice().multiply(BigDecimal.valueOf(quantity));
        Order order = new Order(null, 1, LocalDateTime.now(), status, OrderType.EAT_IN, lineTotal, null);
        OrderItem item = new OrderItem(null, order, product, quantity, product.getBasePrice(), List.of());

        order.setItems(List.of(item));
        orderRepo.save(order);
    }
}