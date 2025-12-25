package com.wheezybaton.kiosk_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa jest wymagana")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Cena jest wymagana")
    @DecimalMin(value = "0.01", message = "Cena musi być większa od 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private boolean available = true;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductIngredient> productIngredients;

    @Column(nullable = false)
    private boolean deleted = false;
}