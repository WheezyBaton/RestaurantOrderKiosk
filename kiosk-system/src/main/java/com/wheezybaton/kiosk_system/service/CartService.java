package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.CartSession;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class CartService {

    private final ProductRepository productRepo;
    private final CartSession cartSession;

    public void addToCart(Long productId,
                          List<Long> addedIngredientIds,
                          List<Long> removedIngredientIds,
                          @Min(value = 1, message = "Quantity must be at least 1") int quantity) {

        log.debug("Request to add to cart -> ProductID: {}, Quantity: {}, AddedIngredients: {}, RemovedIngredients: {}",
                productId, quantity, addedIngredientIds, removedIngredientIds);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> {
                    log.error("Failed to add to cart. Product not found with ID: {}", productId);
                    return new RuntimeException("Product not found");
                });

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

        log.info("Added to cart: {} | Quantity: {} | Unit Price: {} | Total Price: {}",
                item.getProductName(), quantity, finalUnitPrice, item.getTotalPrice());
    }

    public void removeFromCart(UUID itemId) {
        log.debug("Removing cart item with ID: {}", itemId);
        cartSession.removeItem(itemId);
    }

    public CartSession getSession() {
        return cartSession;
    }

    public CartItemDto getCartItem(UUID itemId) {
        log.debug("Retrieving cart item details for ID: {}", itemId);
        return cartSession.getItem(itemId);
    }
}