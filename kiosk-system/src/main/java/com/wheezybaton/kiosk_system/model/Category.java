package com.wheezybaton.kiosk_system.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String imageUrl;

    @OneToMany(mappedBy = "category")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Product> products;
}