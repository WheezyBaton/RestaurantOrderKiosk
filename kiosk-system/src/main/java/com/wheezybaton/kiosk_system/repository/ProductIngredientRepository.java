package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Long> {
}