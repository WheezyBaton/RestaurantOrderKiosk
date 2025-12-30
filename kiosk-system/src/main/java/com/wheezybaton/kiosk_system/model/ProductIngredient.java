package com.wheezybaton.kiosk_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    private Product product;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(nullable = false)
    private boolean isDefault;

    @Column(nullable = false)
    private int displayOrder;

    @Column(precision = 10, scale = 2)
    private BigDecimal customPrice;

    @Column(nullable = false)
    private int maxQuantity = 1;

    public BigDecimal getEffectivePrice() {
        return customPrice != null ? customPrice : ingredient.getPrice();
    }
}