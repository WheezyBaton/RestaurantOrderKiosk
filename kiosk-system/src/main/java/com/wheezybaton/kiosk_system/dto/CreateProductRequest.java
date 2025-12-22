package com.wheezybaton.kiosk_system.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Nazwa produktu nie może być pusta")
    private String name;

    @NotNull(message = "Cena jest wymagana")
    @DecimalMin(value = "0.01", message = "Cena musi być większa od 0")
    private BigDecimal basePrice;

    private String description;
    private String imageUrl;

    @NotNull(message = "Kategoria jest wymagana")
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