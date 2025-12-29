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

        Product product = new Product();
        product.setId(productId);
        product.setName("Configurable Burger");

        Ingredient cheese = new Ingredient(); cheese.setId(10L); cheese.setName("Cheese");
        Ingredient onion = new Ingredient(); onion.setId(20L); onion.setName("Onion");

        ProductIngredient piCheese = new ProductIngredient();
        piCheese.setIngredient(cheese);
        piCheese.setDefault(true);

        ProductIngredient piOnion = new ProductIngredient();
        piOnion.setIngredient(onion);
        piOnion.setDefault(false);

        product.setProductIngredients(List.of(piCheese, piOnion));

        CartItemDto cartItem = new CartItemDto();
        cartItem.setId(cartItemId);
        cartItem.setProductId(productId);
        cartItem.setQuantity(5);
        cartItem.setRemovedIngredientIds(List.of(10L));
        cartItem.setAddedIngredientIds(List.of(20L));

        when(productService.getProductById(productId)).thenReturn(product);
        when(cartService.getCartItem(cartItemId)).thenReturn(cartItem);

        mockMvc.perform(get("/configure")
                        .param("productId", productId.toString())
                        .param("cartItemId", cartItemId.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("cartItemId", cartItemId))
                .andExpect(model().attribute("mainQuantity", 5))
                .andExpect(model().attribute("ingredientQuantities", org.hamcrest.Matchers.hasEntry(10L, 0)))
                .andExpect(model().attribute("ingredientQuantities", org.hamcrest.Matchers.hasEntry(20L, 1)));
    }

    @Test
    void addToCart_ShouldRemoveOldItemAndRedirectToCheckout_WhenUpdating() throws Exception {
        Long productId = 1L;
        UUID oldCartItemId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setBasePrice(BigDecimal.TEN);
        product.setProductIngredients(Collections.emptyList());

        when(productService.getProductById(productId)).thenReturn(product);

        mockMvc.perform(post("/cart/add-custom")
                        .with(csrf())
                        .param("productId", productId.toString())
                        .param("quantity", "1")
                        .param("cartItemId", oldCartItemId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/checkout"));

        verify(cartService).removeFromCart(oldCartItemId);
        verify(cartService).addToCart(eq(productId), anyList(), anyList(), eq(1));
    }

    @Test
    void addToCart_ShouldIgnoreInvalidIngredientQuantity() throws Exception {
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setBasePrice(BigDecimal.TEN);

        Ingredient ing = new Ingredient();
        ing.setId(999L);
        ProductIngredient pi = new ProductIngredient();
        pi.setIngredient(ing);
        pi.setDefault(false);
        product.setProductIngredients(List.of(pi));

        when(productService.getProductById(productId)).thenReturn(product);

        mockMvc.perform(post("/cart/add-custom")
                        .with(csrf())
                        .param("productId", productId.toString())
                        .param("quantity", "1")
                        .param("qty_999", "invalid_number"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/menu"));

        verify(cartService).addToCart(eq(productId), anyList(), anyList(), eq(1));
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