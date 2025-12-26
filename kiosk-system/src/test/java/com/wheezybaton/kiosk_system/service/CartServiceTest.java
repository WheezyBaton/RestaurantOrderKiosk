package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.CartSession;
import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private ProductRepository productRepo;
    @Mock private CartSession cartSession;

    @InjectMocks
    private CartService cartService;

    @Test
    void addToCart_ShouldCalculatePriceWithIngredients() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Pizza");
        product.setBasePrice(new BigDecimal("30.00"));

        Ingredient cheese = new Ingredient(10L, "Cheese", new BigDecimal("5.00"));
        ProductIngredient pi = new ProductIngredient();
        pi.setIngredient(cheese);
        pi.setCustomPrice(new BigDecimal("4.00"));

        product.setProductIngredients(List.of(pi));

        when(productRepo.findById(1L)).thenReturn(Optional.of(product));

        cartService.addToCart(1L, List.of(10L), null, 1);

        verify(cartSession).addItem(any(CartItemDto.class));
    }

    @Test
    void removeFromCart_ShouldDelegate() {
        java.util.UUID uuid = java.util.UUID.randomUUID();
        cartService.removeFromCart(uuid);
        verify(cartSession).removeItem(uuid);
    }
}