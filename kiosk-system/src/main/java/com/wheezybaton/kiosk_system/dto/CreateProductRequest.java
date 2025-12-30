package com.wheezybaton.kiosk_system.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Nazwa produktu nie może być pusta")
    @Size(min = 3, max = 100, message = "Nazwa produktu musi mieć od 3 do 100 znaków")
    private String name;

    @NotNull(message = "Cena jest wymagana")
    @DecimalMin(value = "0.01", message = "Cena musi być większa od 0")
    private BigDecimal basePrice;

    @Size(max = 1000, message = "Opis nie może przekraczać 1000 znaków")
    private String description;

    private String imageUrl;

    @NotNull(message = "Kategoria jest wymagana")
    private Long categoryId;

    private List<IngredientConfig> ingredients;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientConfig {
        private Long ingredientId;
        private boolean isDefault;
        private Integer maxQuantity;
        private BigDecimal customPrice;
    }
}