package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.CategoryRepository;
import com.wheezybaton.kiosk_system.service.CartService;
import com.wheezybaton.kiosk_system.service.OrderService;
import com.wheezybaton.kiosk_system.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KioskController.class)
class KioskControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ProductService productService;
    @MockitoBean private CategoryRepository categoryRepo;
    @MockitoBean private CartService cartService;
    @MockitoBean private OrderService orderService;

    @Test
    @WithMockUser
    void showMenu_ShouldPassCartAndProducts() throws Exception {
        when(cartService.getSession()).thenReturn(new CartSession());
        when(productService.getAvailableProducts()).thenReturn(List.of(new Product()));

        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("menu"))
                .andExpect(model().attributeExists("products", "cart"));
    }

    @Test
    @WithMockUser
    void addCustomProduct_ShouldProcessIngredientsFromRequest() throws Exception {
        Product product = new Product();
        product.setId(1L);

        Ingredient ing1 = new Ingredient(); ing1.setId(10L);
        Ingredient ing2 = new Ingredient(); ing2.setId(20L);

        ProductIngredient pi1 = new ProductIngredient(); pi1.setIngredient(ing1); pi1.setDefault(true);
        ProductIngredient pi2 = new ProductIngredient(); pi2.setIngredient(ing2); pi2.setDefault(false);

        product.setProductIngredients(List.of(pi1, pi2));

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(post("/cart/add-custom")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "1")
                        .param("qty_10", "0")
                        .param("qty_20", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/menu"));

        verify(cartService).addToCart(
                eq(1L),
                argThat(list -> list.contains(20L)),
                argThat(list -> list.contains(10L)),
                eq(1)
        );
    }

    @Test
    @WithMockUser
    void removeCartItem_ShouldCallService() throws Exception {
        UUID uuid = UUID.randomUUID();
        mockMvc.perform(post("/cart/remove/" + uuid).with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(cartService).removeFromCart(uuid);
    }

    @Test
    @WithMockUser
    void payAndOrder_ShouldRedirectToSuccess_OnSuccess() throws Exception {
        Order order = new Order();
        order.setDailyNumber(123);
        when(orderService.placeOrder()).thenReturn(order);

        mockMvc.perform(post("/order/pay").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order-success"))
                .andExpect(flash().attribute("orderNumber", 123));
    }
}