package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.service.CartService;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class KioskController {

    private final ProductRepository productRepo;
    private final CartService cartService;

    @GetMapping("/")
    public String showMenu(Model model) {
        model.addAttribute("products", productRepo.findAll());
        model.addAttribute("cart", cartService.getSession());
        return "menu";
    }

    @PostMapping("/cart/quick-add")
    public String quickAdd(@RequestParam Long productId) {
        cartService.addToCart(productId, Collections.emptyList(), Collections.emptyList(), 1);
        return "redirect:/";
    }

    @PostMapping("/cart/clear")
    public String clearCart() {
        cartService.getSession().clear();
        return "redirect:/";
    }

    @GetMapping("/configure")
    public String showConfiguration(@RequestParam Long productId, Model model) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono produktu"));
        model.addAttribute("product", product);
        return "configure";
    }

    @PostMapping("/cart/add-custom")
    public String addCustomProduct(
            @RequestParam Long productId,
            @RequestParam(required = false) List<Long> selectedIngredientIds,
            @RequestParam int quantity
    ) {
        if (selectedIngredientIds == null) {
            selectedIngredientIds = Collections.emptyList();
        }

        Product product = productRepo.findById(productId).orElseThrow();

        List<Long> addedIds = new ArrayList<>();
        List<Long> removedIds = new ArrayList<>();

        for (ProductIngredient config : product.getProductIngredients()) {
            Long ingId = config.getIngredient().getId();
            boolean isSelected = selectedIngredientIds.contains(ingId);

            if (config.isDefault() && !isSelected) {
                removedIds.add(ingId);
            }

            if (!config.isDefault() && isSelected) {
                addedIds.add(ingId);
            }
        }
        cartService.addToCart(productId, addedIds, removedIds, quantity);
        return "redirect:/";
    }
}