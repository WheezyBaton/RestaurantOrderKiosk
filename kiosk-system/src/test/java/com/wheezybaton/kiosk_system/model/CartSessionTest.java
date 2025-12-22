package com.wheezybaton.kiosk_system.model;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CartSessionTest {

    @Test
    void shouldHandleCartOperations() {
        CartSession session = new CartSession();

        session.setOrderType(OrderType.TAKE_AWAY);
        assertEquals(OrderType.TAKE_AWAY, session.getOrderType());

        CartItemDto item1 = new CartItemDto();
        item1.setId(UUID.randomUUID());
        item1.setTotalPrice(BigDecimal.TEN);

        CartItemDto item2 = new CartItemDto();
        item2.setId(UUID.randomUUID());
        item2.setTotalPrice(BigDecimal.valueOf(20));

        session.addItem(item1);
        session.addItem(item2);

        assertEquals(2, session.getItems().size());
        assertEquals(BigDecimal.valueOf(30), session.getTotalCartValue());

        session.removeItem(item1.getId());
        assertEquals(1, session.getItems().size());
        assertEquals(BigDecimal.valueOf(20), session.getTotalCartValue());

        session.clear();
        assertTrue(session.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, session.getTotalCartValue());
    }
}