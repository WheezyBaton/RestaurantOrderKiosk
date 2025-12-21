package com.wheezybaton.kiosk_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    private UUID id = UUID.randomUUID();
    private Long productId;
    private String productName;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private int quantity;

    private List<String> addedIngredients = new ArrayList<>();
    private List<String> removedIngredients = new ArrayList<>();

    private List<Long> addedIngredientIds = new ArrayList<>();
    private List<Long> removedIngredientIds = new ArrayList<>();

    public void recalculateTotal() {
        if (unitPrice != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}