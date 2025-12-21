package com.wheezybaton.kiosk_system.config;

import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final IngredientRepository ingredientRepo;
    private final ProductIngredientRepository productIngredientRepo;

    @Override
    public void run(String... args) throws Exception {
        productIngredientRepo.deleteAll();
        productRepo.deleteAll();
        ingredientRepo.deleteAll();
        categoryRepo.deleteAll();

        Category burgers = new Category(null, "Burgers", "burger.png", null);
        Category drinks = new Category(null, "Drinks", "drink.png", null);
        categoryRepo.saveAll(List.of(burgers, drinks));

        Ingredient bun = new Ingredient(null, "Brioche Bun", new BigDecimal("0.00"));
        Ingredient meat = new Ingredient(null, "100% Beef Patty", new BigDecimal("5.00"));
        Ingredient cheese = new Ingredient(null, "Cheddar Cheese", new BigDecimal("2.00"));
        Ingredient onion = new Ingredient(null, "Onion", new BigDecimal("0.00"));
        Ingredient bacon = new Ingredient(null, "Bacon", new BigDecimal("3.00"));

        ingredientRepo.saveAll(List.of(bun, meat, cheese, onion, bacon));

        Product classicBurger = new Product(
                null,
                "Classic Burger",
                new BigDecimal("25.00"),
                "Classic beef burger with cheese",
                "classic.jpg",
                burgers,
                null,
                false
        );
        productRepo.save(classicBurger);

        ProductIngredient pi1 = new ProductIngredient(null, classicBurger, bun, true, 1, null, 1);
        ProductIngredient pi2 = new ProductIngredient(null, classicBurger, meat, true, 2, null, 2);
        ProductIngredient pi3 = new ProductIngredient(null, classicBurger, cheese, true, 3, null, 2);
        ProductIngredient pi4 = new ProductIngredient(null, classicBurger, onion, true, 4, null, 1);
        ProductIngredient pi5 = new ProductIngredient(null, classicBurger, bacon, false, 5, null, 3);

        productIngredientRepo.saveAll(List.of(pi1, pi2, pi3, pi4, pi5));

        System.out.println("Database initialized!");
    }
}