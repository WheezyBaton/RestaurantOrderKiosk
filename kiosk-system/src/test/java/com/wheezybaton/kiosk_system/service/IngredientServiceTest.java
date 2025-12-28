package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientService ingredientService;

    @Test
    void shouldReturnAllIngredients() {
        when(ingredientRepository.findAll()).thenReturn(List.of(new Ingredient()));
        assertThat(ingredientService.getAllIngredients()).hasSize(1);
    }

    @Test
    void shouldGetIngredientById() {
        Ingredient ing = new Ingredient();
        ing.setId(1L);
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ing));

        Ingredient result = ingredientService.getIngredientById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void shouldSaveIngredient() {
        Ingredient ing = new Ingredient();
        ingredientService.saveIngredient(ing);
        verify(ingredientRepository).save(ing);
    }

    @Test
    void shouldDeleteIngredient() {
        ingredientService.deleteIngredient(1L);
        verify(ingredientRepository).deleteById(1L);
    }
}