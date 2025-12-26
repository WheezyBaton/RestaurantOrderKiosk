package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Ingredient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class IngredientRepositoryTest {

    @Autowired
    private IngredientRepository ingredientRepo;

    @Test
    void shouldSaveAndFindIngredient() {
        Ingredient ing = new Ingredient(null, "Test Mayo", BigDecimal.valueOf(2.50));

        Ingredient saved = ingredientRepo.save(ing);

        assertThat(saved.getId()).isNotNull();
        Optional<Ingredient> found = ingredientRepo.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Mayo");
    }

    @Test
    void shouldDeleteIngredient() {
        Ingredient saved = ingredientRepo.save(new Ingredient(null, "Delete Me", BigDecimal.ONE));

        ingredientRepo.deleteById(saved.getId());

        assertThat(ingredientRepo.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenNameIsNotUnique() {
        ingredientRepo.save(new Ingredient(null, "Unique Sauce", BigDecimal.ONE));

        assertThrows(DataIntegrityViolationException.class, () -> {
            ingredientRepo.saveAndFlush(new Ingredient(null, "Unique Sauce", BigDecimal.TEN));
        });
    }
}