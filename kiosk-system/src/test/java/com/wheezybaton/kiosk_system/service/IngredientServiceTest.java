package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.exception.ResourceNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void shouldGetIngredientById_WhenExists() {
        Ingredient ing = new Ingredient();
        ing.setId(1L);
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ing));

        Ingredient result = ingredientService.getIngredientById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getIngredientById_WhenNotFound_ShouldThrowException() {
        when(ingredientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredientService.getIngredientById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldSaveIngredient() {
        Ingredient ing = new Ingredient();
        Ingredient savedIng = new Ingredient();
        savedIng.setId(10L);

        when(ingredientRepository.save(ing)).thenReturn(savedIng);

        Ingredient result = ingredientService.saveIngredient(ing);

        verify(ingredientRepository).save(ing);
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void shouldDeleteIngredient() {
        ingredientService.deleteIngredient(1L);
        verify(ingredientRepository).deleteById(1L);
    }
}