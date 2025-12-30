package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.CategoryRepository;
import com.wheezybaton.kiosk_system.service.CartService;
import com.wheezybaton.kiosk_system.service.OrderService;
import com.wheezybaton.kiosk_system.service.ProductService;
import com.wheezybaton.kiosk_system.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KioskController.class)
@Import(SecurityConfig.class)
class KioskControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ProductService productService;
    @MockitoBean private CategoryRepository categoryRepo;
    @MockitoBean private CartService cartService;
    @MockitoBean private OrderService orderService;

    @Test
    void showMenu_ShouldPassCartAndProducts() throws Exception {
        when(cartService.getSession()).thenReturn(new CartSession());
        when(productService.getAvailableProducts()).thenReturn(List.of(new Product()));

        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("menu"))
                .andExpect(model().attributeExists("products", "cart"));
    }

    @Test
    void addCustomProduct_ShouldProcessIngredientsFromRequest() throws Exception {
        ProductIngredient pi1 = new ProductIngredient(null, null, new Ingredient(10L, null, null), true, 0, null, 1);
        ProductIngredient pi2 = new ProductIngredient(null, null, new Ingredient(20L, null, null), false, 0, null, 1);

        Product product = new Product(1L, null, null, null, null, true, null, List.of(pi1, pi2), false);

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
    void removeCartItem_ShouldCallService() throws Exception {
        UUID uuid = UUID.randomUUID();
        mockMvc.perform(post("/cart/remove/" + uuid).with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(cartService).removeFromCart(uuid);
    }

    @Test
    void payAndOrder_ShouldRedirectToSuccess_OnSuccess() throws Exception {
        Order order = new Order();
        order.setDailyNumber(123);
        when(orderService.placeOrder()).thenReturn(order);

        mockMvc.perform(post("/order/pay").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/order-success"))
                .andExpect(flash().attribute("orderNumber", 123));
    }

    @Test
    void configureProduct_ShouldLoadConfigFromCart_WhenCartItemIdProvided() throws Exception {
        Long productId = 1L;
        UUID cartItemId = UUID.randomUUID();

        ProductIngredient piCheese = new ProductIngredient(null, null, new Ingredient(10L, "Cheese", null), true, 0, null, 1);
        ProductIngredient piOnion = new ProductIngredient(null, null, new Ingredient(20L, "Onion", null), false, 0, null, 1);
        Product product = new Product(productId, "Configurable Burger", null, null, null, true, null, List.of(piCheese, piOnion), false);

        CartItemDto cartItem = new CartItemDto(cartItemId, productId, null, null, null, 5, List.of(), List.of(), List.of(20L), List.of(10L));

        when(productService.getProductById(productId)).thenReturn(product);
        when(cartService.getCartItem(cartItemId)).thenReturn(cartItem);

        mockMvc.perform(get("/configure")
                        .param("productId", productId.toString())
                        .param("cartItemId", cartItemId.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cartItemId", cartItemId))
                .andExpect(model().attribute("mainQuantity", 5))
                .andExpect(model().attribute("ingredientQuantities", hasEntry(10L, 0)))
                .andExpect(model().attribute("ingredientQuantities", hasEntry(20L, 1)));
    }

    @Test
    void addToCart_ShouldRemoveOldItemAndRedirectToCheckout_WhenUpdating() throws Exception {
        UUID oldCartItemId = UUID.randomUUID();
        Product product = new Product(1L, null, BigDecimal.TEN, null, null, true, null, List.of(), false);

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(post("/cart/add-custom")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "1")
                        .param("cartItemId", oldCartItemId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/checkout"));

        verify(cartService).removeFromCart(oldCartItemId);
        verify(cartService).addToCart(eq(1L), anyList(), anyList(), eq(1));
    }

    @Test
    void addToCart_ShouldIgnoreInvalidIngredientQuantity() throws Exception {
        ProductIngredient pi = new ProductIngredient(null, null, new Ingredient(999L, null, null), false, 0, null, 1);
        Product product = new Product(1L, null, BigDecimal.TEN, null, null, true, null, List.of(pi), false);

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(post("/cart/add-custom")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "1")
                        .param("qty_999", "invalid_number"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/menu"));

        verify(cartService).addToCart(eq(1L), anyList(), anyList(), eq(1));
    }

    @Test
    void clearCart_ShouldClearSessionAndRedirect() throws Exception {
        CartSession mockSession = mock(CartSession.class);
        when(cartService.getSession()).thenReturn(mockSession);

        mockMvc.perform(post("/cart/clear")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        verify(mockSession).clear();
    }

    @Test
    void showCheckout_ShouldRedirectToMenu_WhenCartIsEmpty() throws Exception {
        CartSession mockSession = mock(CartSession.class);
        when(cartService.getSession()).thenReturn(mockSession);
        when(mockSession.getItems()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/menu"));
    }

    @Test
    void showSuccess_ShouldRedirectToWelcome_WhenNoOrderNumber() throws Exception {
        mockMvc.perform(get("/order-success"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void showSuccess_ShouldShowPage_WhenOrderNumberExists() throws Exception {
        mockMvc.perform(get("/order-success")
                        .flashAttr("orderNumber", 123L))
                .andExpect(status().isOk())
                .andExpect(view().name("success"))
                .andExpect(model().attributeExists("orderNumber"));
    }
}