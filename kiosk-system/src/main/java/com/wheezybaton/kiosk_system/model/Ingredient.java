package com.wheezybaton.kiosk_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa składnika nie może być pusta")
    @Size(min = 2, max = 50, message = "Nazwa składnika musi mieć od 2 do 50 znaków")
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull(message = "Cena jest wymagana")
    @DecimalMin(value = "0.00", message = "Cena nie może być ujemna")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}