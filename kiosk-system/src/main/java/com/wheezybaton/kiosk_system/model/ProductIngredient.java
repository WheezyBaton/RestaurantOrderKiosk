package com.wheezybaton.kiosk_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    private boolean isDefault;
    private int displayOrder;
    private BigDecimal customPrice;
    private int maxQuantity = 1;

    public BigDecimal getEffectivePrice() {
        return customPrice != null ? customPrice : ingredient.getPrice();
    }
}