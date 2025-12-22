package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.service.CartService;
import com.wheezybaton.kiosk_system.service.OrderService;
import com.wheezybaton.kiosk_system.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    @MockitoBean private CartService cartService;
    @MockitoBean private OrderService orderService;
    @MockitoBean private CartSession cartSession;

    @BeforeEach
    void setUp() {
        when(cartService.getSession()).thenReturn(cartSession);
        when(cartSession.getItems()).thenReturn(new ArrayList<>());
        when(cartSession.getOrderType()).thenReturn(OrderType.EAT_IN);
    }

    @Test
    @WithMockUser
    void shouldShowMenu() throws Exception {
        Category cat = new Category();
        cat.setName("Drinks");
        Product prod = new Product();
        prod.setName("Cola");
        prod.setCategory(cat);
        prod.setBasePrice(BigDecimal.valueOf(5));

        when(productService.getAllProducts()).thenReturn(List.of(prod));

        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("menu"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    @WithMockUser
    void shouldProcessCustomProductAddition() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setBasePrice(BigDecimal.TEN);

        Ingredient ing = new Ingredient(10L, "Cheese", BigDecimal.ONE);
        ProductIngredient pi = new ProductIngredient();
        pi.setIngredient(ing);
        pi.setDefault(false);
        p.setProductIngredients(List.of(pi));

        when(productService.getProductById(1L)).thenReturn(p);

        mockMvc.perform(post("/cart/add-custom")
                        .param("productId", "1")
                        .param("quantity", "1")
                        .param("qty_10", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(cartService).addToCart(eq(1L), anyList(), anyList(), eq(1));
    }

    @Test
    @WithMockUser
    void shouldPlaceOrder() throws Exception {
        Order o = new Order();
        o.setDailyNumber(55);
        when(orderService.placeOrder()).thenReturn(o);

        mockMvc.perform(post("/order/pay").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("orderNumber", 55));
    }

    @Test @WithMockUser
    void menu() throws Exception {
        mockMvc.perform(get("/menu")).andExpect(status().isOk());
    }

    @Test @WithMockUser
    void checkout() throws Exception {
        when(cartSession.getItems()).thenReturn(List.of(new com.wheezybaton.kiosk_system.dto.CartItemDto()));
        mockMvc.perform(get("/checkout")).andExpect(status().isOk());
    }
}