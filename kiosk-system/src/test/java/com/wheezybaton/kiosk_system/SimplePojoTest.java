package com.wheezybaton.kiosk_system;

import com.wheezybaton.kiosk_system.dto.*;
import com.wheezybaton.kiosk_system.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SimplePojoTest {

    @Test
    void testModelsAndDtosForCoverage() {
        Order order = new Order();
        order.setId(1L);
        order.setDailyNumber(55);
        order.setStatus(OrderStatus.NEW);
        order.setType(OrderType.EAT_IN);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());
        assertNotNull(order.toString());
        assertNotNull(order.getId());

        Ingredient ing = new Ingredient(1L, "Test", BigDecimal.ONE);
        ing.setName("New Name");
        assertNotNull(ing.getName());
        assertNotNull(ing.toString());

        SalesStatDto stat = new SalesStatDto("Burger", 10L, BigDecimal.TEN);
        stat.setProductName("New Burger");
        assertNotNull(stat.getProductName());
        assertNotNull(stat.getTotalRevenue());
        assertNotNull(stat.toString());

        CreateProductRequest req = new CreateProductRequest();
        req.setName("Burger");
        req.setBasePrice(BigDecimal.TEN);
        req.setIngredients(new ArrayList<>());
        assertNotNull(req.getName());
        assertNotNull(req.toString());

        assertNotNull(OrderType.EAT_IN.getDisplayName());
        assertNotNull(ModifierAction.ADDED);

        ProductIngredient pi = new ProductIngredient();
        pi.setId(1L);
        pi.setCustomPrice(BigDecimal.ONE);
        pi.setDefault(true);
        assertNotNull(pi.getId());
        assertNotNull(pi.toString());
    }
}