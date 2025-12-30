package com.wheezybaton.kiosk_system.model;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@SessionScope
@Getter
@Setter
@ToString
public class CartSession {
    private static final BigDecimal PACKAGING_FEE = new BigDecimal("1.00");

    private List<CartItemDto> items = new ArrayList<>();
    private OrderType orderType = OrderType.EAT_IN;

    public BigDecimal getTotalCartValue() {
        if (items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = items.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (this.orderType == OrderType.TAKE_AWAY) {
            return sum.add(PACKAGING_FEE);
        }

        return sum;
    }

    public BigDecimal getPackagingFee() {
        return this.orderType == OrderType.TAKE_AWAY ? PACKAGING_FEE : BigDecimal.ZERO;
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

    public CartItemDto getItem(UUID itemId) {
        return items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }
}