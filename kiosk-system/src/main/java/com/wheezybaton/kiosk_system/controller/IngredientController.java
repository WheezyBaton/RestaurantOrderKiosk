package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.service.IngredientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    public String listIngredients(Model model) {
        log.debug("Request received to list all ingredients.");

        List<Ingredient> ingredients = ingredientService.getAllIngredients();
        model.addAttribute("ingredients", ingredients);

        log.debug("Loaded {} ingredients to display.", ingredients.size());
        return "admin/ingredients";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("ingredient", new Ingredient());
        return "admin/ingredient-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.debug("Request to edit ingredient with ID: {}", id);

        Ingredient ingredient = ingredientService.getIngredientById(id);

        if (ingredient == null) {
            log.error("Edit failed. Ingredient not found with ID: {}", id);
            throw new RuntimeException("Ingredient not found");
        }

        model.addAttribute("ingredient", ingredient);
        return "admin/ingredient-form";
    }

    @PostMapping("/save")
    public String saveIngredient(@Valid @ModelAttribute Ingredient ingredient,
                                 BindingResult bindingResult) {
        log.debug("Attempting to save ingredient. Name: {}, ID: {}", ingredient.getName(), ingredient.getId());

        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for ingredient: {}", bindingResult.getAllErrors());
            return "admin/ingredient-form";
        }

        Ingredient savedIngredient = ingredientService.saveIngredient(ingredient);

        log.info("Ingredient saved successfully: {} (ID: {})", savedIngredient.getName(), savedIngredient.getId());
        return "redirect:/admin/ingredients";
    }

    @GetMapping("/delete/{id}")
    public String deleteIngredient(@PathVariable Long id) {
        log.debug("Request to delete ingredient with ID: {}", id);
        try {
            ingredientService.deleteIngredient(id);
            log.info("Ingredient deleted successfully: ID {}", id);
        } catch (Exception e) {
            log.warn("Failed to delete ingredient ID: {}. It might be in use by products. Error: {}", id, e.getMessage());
            return "redirect:/admin/ingredients?error=used";
        }
        return "redirect:/admin/ingredients";
    }
}