package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientRepository ingredientRepo;

    @GetMapping
    public String listIngredients(Model model) {
        model.addAttribute("ingredients", ingredientRepo.findAll());
        return "admin/ingredients";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("ingredient", new Ingredient());
        return "admin/ingredient-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Ingredient ingredient = ingredientRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono składnika"));
        model.addAttribute("ingredient", ingredient);
        return "admin/ingredient-form";
    }

    @PostMapping("/save")
    public String saveIngredient(@ModelAttribute Ingredient ingredient) {
        ingredientRepo.save(ingredient);
        return "redirect:/admin/ingredients";
    }

    @GetMapping("/delete/{id}")
    public String deleteIngredient(@PathVariable Long id) {
        try {
            ingredientRepo.deleteById(id);
        } catch (Exception e) {
            return "redirect:/admin/ingredients?error=used";
        }
        return "redirect:/admin/ingredients";
    }
}