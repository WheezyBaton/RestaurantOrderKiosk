package com.wheezybaton.kiosk_system.model;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CartSessionTest {

    private CartSession session;

    @BeforeEach
    void setUp() {
        session = new CartSession();
    }

    private CartItemDto createItem(BigDecimal price) {
        return new CartItemDto(UUID.randomUUID(), null, null, null, price, 0, List.of(), List.of(), List.of(), List.of());
    }

    @Test
    void addItem_ShouldIncreaseListSize() {
        CartItemDto item = createItem(BigDecimal.TEN);

        session.addItem(item);

        assertEquals(1, session.getItems().size());
        assertEquals(item, session.getItem(item.getId()));
    }

    @Test
    void removeItem_ShouldDecreaseListSize() {
        CartItemDto item = createItem(BigDecimal.TEN);
        session.addItem(item);

        session.removeItem(item.getId());

        assertTrue(session.getItems().isEmpty());
    }

    @Test
    void clear_ShouldRemoveAllItems() {
        session.addItem(createItem(BigDecimal.TEN));
        session.addItem(createItem(BigDecimal.ONE));

        session.clear();

        assertTrue(session.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, session.getTotalCartValue());
    }

    @Test
    void getTotalCartValue_ShouldSumItemsOnly_WhenEatIn() {
        session.setOrderType(OrderType.EAT_IN);
        session.addItem(createItem(new BigDecimal("10.00")));
        session.addItem(createItem(new BigDecimal("20.00")));

        BigDecimal total = session.getTotalCartValue();

        assertEquals(new BigDecimal("30.00"), total);
        assertEquals(BigDecimal.ZERO, session.getPackagingFee());
    }

    @Test
    void getTotalCartValue_ShouldAddPackagingFee_WhenTakeAway() {
        session.setOrderType(OrderType.TAKE_AWAY);
        session.addItem(createItem(new BigDecimal("10.00")));
        session.addItem(createItem(new BigDecimal("20.00")));

        BigDecimal total = session.getTotalCartValue();

        assertEquals(new BigDecimal("31.00"), total);
        assertEquals(new BigDecimal("1.00"), session.getPackagingFee());
    }

    @Test
    void getTotalCartValue_ShouldBeZero_WhenCartIsEmptyAndTakeAway() {
        session.setOrderType(OrderType.TAKE_AWAY);

        BigDecimal total = session.getTotalCartValue();

        assertEquals(BigDecimal.ZERO, total);
    }
}