package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    public Ingredient getIngredientById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));
    }

    @Transactional
    public Ingredient saveIngredient(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }

    @Transactional
    public void deleteIngredient(Long id) {
        ingredientRepository.deleteById(id);
    }
}