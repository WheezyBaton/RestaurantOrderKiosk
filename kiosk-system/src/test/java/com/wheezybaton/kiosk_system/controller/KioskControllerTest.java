package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.config.SecurityConfig;
import com.wheezybaton.kiosk_system.model.CartSession;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import com.wheezybaton.kiosk_system.service.CartService;
import com.wheezybaton.kiosk_system.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KioskController.class)
@Import(SecurityConfig.class)
class KioskControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ProductRepository productRepo;
    @MockitoBean private CartService cartService;
    @MockitoBean private OrderService orderService;
    @MockitoBean private CartSession cartSession;

    @Test
    void showWelcome_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"));
    }

    @Test
    void showMenu_ShouldReturnMenuView() throws Exception {
        when(cartService.getSession()).thenReturn(new CartSession());
        when(productRepo.findByDeletedFalse()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("menu"));
    }

    @Test
    void selectOrderType_ShouldRedirectToMenu() throws Exception {
        when(cartService.getSession()).thenReturn(new CartSession());

        mockMvc.perform(post("/select-type")
                        .param("type", "TAKE_AWAY")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/menu"));
    }

    @Test
    void configureProduct_ShouldReturnConfigView() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setName("Burger");
        p.setBasePrice(BigDecimal.TEN);

        when(productRepo.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/configure").param("productId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("configure"))
                .andExpect(model().attributeExists("product"));
    }
}