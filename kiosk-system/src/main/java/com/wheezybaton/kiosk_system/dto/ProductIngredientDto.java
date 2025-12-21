package com.wheezybaton.kiosk_system.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductIngredientDto {
    private Long ingredientId;
    private String name;
    private BigDecimal price;
    private boolean isDefault;
    private int maxQuantity;
}