package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.CartSession;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final ProductRepository productRepo;
    private final CartSession cartSession;

    public void addToCart(Long productId, List<Long> addedIngredientIds, List<Long> removedIngredientIds, int quantity) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Map<Long, ProductIngredient> configMap = product.getProductIngredients().stream()
                .collect(Collectors.toMap(pi -> pi.getIngredient().getId(), pi -> pi));

        BigDecimal extrasCost = BigDecimal.ZERO;
        List<String> addedNames = new ArrayList<>();

        if (addedIngredientIds != null) {
            for (Long ingId : addedIngredientIds) {
                ProductIngredient config = configMap.get(ingId);
                if (config != null) {
                    extrasCost = extrasCost.add(config.getEffectivePrice());
                    addedNames.add(config.getIngredient().getName());
                }
            }
        }

        List<String> removedNames = new ArrayList<>();
        if (removedIngredientIds != null) {
            for (Long ingId : removedIngredientIds) {
                ProductIngredient config = configMap.get(ingId);
                if (config != null) {
                    removedNames.add(config.getIngredient().getName());
                }
            }
        }
        BigDecimal finalUnitPrice = product.getBasePrice().add(extrasCost);

        CartItemDto item = new CartItemDto();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setUnitPrice(finalUnitPrice);
        item.setQuantity(quantity);
        item.recalculateTotal();
        item.setAddedIngredients(addedNames);
        item.setRemovedIngredients(removedNames);
        item.setAddedIngredientIds(addedIngredientIds != null ? addedIngredientIds : new ArrayList<>());
        item.setRemovedIngredientIds(removedIngredientIds != null ? removedIngredientIds : new ArrayList<>());

        cartSession.addItem(item);
        System.out.println("Dodano do koszyka: " + item.getProductName() + " | Ilość: " + quantity + " | Cena jedn: " + finalUnitPrice);
    }

    public void removeFromCart(UUID itemId) {
        cartSession.removeItem(itemId);
    }

    public CartSession getSession() {
        return cartSession;
    }

    public CartItemDto getCartItem(UUID itemId) {
        return cartSession.getItem(itemId);
    }
}