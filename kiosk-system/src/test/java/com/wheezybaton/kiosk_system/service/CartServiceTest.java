package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private ProductRepository productRepo;
    @Mock private CartSession cartSession;
    @InjectMocks private CartService cartService;

    @Test
    void addToCart_ShouldAddProductWithCorrectPrice() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Burger");
        product.setBasePrice(new BigDecimal("20.00"));
        product.setProductIngredients(Collections.emptyList());

        when(productRepo.findById(1L)).thenReturn(Optional.of(product));

        cartService.addToCart(1L, Collections.emptyList(), Collections.emptyList(), 2);

        ArgumentCaptor<CartItemDto> captor = ArgumentCaptor.forClass(CartItemDto.class);
        verify(cartSession).addItem(captor.capture());

        CartItemDto item = captor.getValue();
        assertEquals("Burger", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("40.00"), item.getTotalPrice());
    }

    @Test
    void removeFromCart_ShouldCallSessionRemove() {
        UUID uuid = UUID.randomUUID();
        cartService.removeFromCart(uuid);
        verify(cartSession).removeItem(uuid);
    }

    @Test
    void getSession_ShouldReturnSession() {
        assertEquals(cartSession, cartService.getSession());
    }
}