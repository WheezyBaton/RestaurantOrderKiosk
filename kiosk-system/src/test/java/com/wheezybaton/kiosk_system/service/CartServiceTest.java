package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.CartSession;
import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        ProductIngredient pi = new ProductIngredient(null, null, new Ingredient(10L, "Cheese", new BigDecimal("5.00")), false, 0, new BigDecimal("4.00"), 1);
        Product product = new Product(1L, "Pizza", new BigDecimal("30.00"), null, null, true, null, List.of(pi), false);

        when(productRepo.findById(1L)).thenReturn(Optional.of(product));

        cartService.addToCart(1L, List.of(10L), null, 2);

        ArgumentCaptor<CartItemDto> captor = ArgumentCaptor.forClass(CartItemDto.class);
        verify(cartSession).addItem(captor.capture());

        CartItemDto capturedItem = captor.getValue();

        assertThat(capturedItem.getProductId()).isEqualTo(1L);
        assertThat(capturedItem.getQuantity()).isEqualTo(2);
        assertThat(capturedItem.getTotalPrice()).isEqualByComparingTo(new BigDecimal("68.00"));
    }
}