package com.wheezybaton.kiosk_system.model;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@SessionScope
@Data
public class CartSession {

    private List<CartItemDto> items = new ArrayList<>();
    private OrderType orderType = OrderType.EAT_IN;

    public BigDecimal getTotalCartValue() {
        return items.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(CartItemDto item) {
        items.add(item);
    }

    public void removeItem(UUID itemId) {
        items.removeIf(i -> i.getId().equals(itemId));
    }

    public void clear() {
        items.clear();
    }
}