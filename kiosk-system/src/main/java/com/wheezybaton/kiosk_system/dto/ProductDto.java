package com.wheezybaton.kiosk_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private BigDecimal basePrice;
    private String description;
    private String imageUrl;
    private String categoryName;
    private List<ProductIngredientDto> ingredients;
}