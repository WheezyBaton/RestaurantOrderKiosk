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
    void shouldCorrectlyMapSalesStatsUsingRowMapper() {
        Product burger = productRepo.save(new Product(null, "Test Burger", BigDecimal.valueOf(20), "", "", true, null, null, false));

        Order order = new Order();
        order.setDailyNumber(1);
        order.setStatus(OrderStatus.COMPLETED);
        order.setType(OrderType.EAT_IN);
        order.setTotalAmount(BigDecimal.valueOf(40));
        order.setCreatedAt(LocalDateTime.now());

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(burger);
        item.setQuantity(2);
        item.setPriceAtPurchase(BigDecimal.valueOf(20));

        order.setItems(List.of(item));
        orderRepo.save(order);

        List<SalesStatDto> stats = statsService.getSalesStats();
        
        assertThat(stats).isNotEmpty();
        SalesStatDto stat = stats.stream()
                .filter(s -> s.getProductName().equals("Test Burger"))
                .findFirst()
                .orElseThrow();

        assertThat(stat.getTotalQuantity()).isEqualTo(2);
        assertThat(stat.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(40.00));
    }
}