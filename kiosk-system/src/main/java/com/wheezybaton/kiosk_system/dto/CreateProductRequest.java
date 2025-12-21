package com.wheezybaton.kiosk_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {
    private String name;
    private BigDecimal basePrice;
    private String description;
    private String imageUrl;
    private Long categoryId;
    private List<IngredientConfig> ingredients;

    @Data
    public static class IngredientConfig {
        private Long ingredientId;
        private boolean isDefault;
        private Integer maxQuantity;
        private BigDecimal customPrice;
    }
}