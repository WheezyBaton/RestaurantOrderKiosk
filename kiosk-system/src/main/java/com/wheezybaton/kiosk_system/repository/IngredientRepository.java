package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}