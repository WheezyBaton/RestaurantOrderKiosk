package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.CartSession;
import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepo;
    private final IngredientRepository ingredientRepo;
    private final CartSession cartSession;

    public void addToCart(Long productId, List<Long> addedIngredientIds, List<Long> removedIngredientIds, int quantity) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<Ingredient> extraIngredients = ingredientRepo.findAllById(addedIngredientIds);

        List<Ingredient> removedIngredients = ingredientRepo.findAllById(removedIngredientIds);

        BigDecimal extrasCost = extraIngredients.stream()
                .map(Ingredient::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalUnitPrice = product.getBasePrice().add(extrasCost);

        CartItemDto item = new CartItemDto();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setUnitPrice(finalUnitPrice);
        item.setQuantity(quantity);
        item.recalculateTotal();
        item.setAddedIngredients(extraIngredients.stream().map(Ingredient::getName).toList());
        item.setRemovedIngredients(removedIngredients.stream().map(Ingredient::getName).toList());

        cartSession.addItem(item);

        System.out.println("🛒 Dodano do koszyka: " + item.getProductName() + " | Suma: " + item.getTotalPrice());
    }

    public void removeFromCart(UUID itemId) {
        cartSession.removeItem(itemId);
    }

    public CartSession getSession() {
        return cartSession;
    }
}