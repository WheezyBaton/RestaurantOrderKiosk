package com.wheezybaton.kiosk_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.*;
import com.wheezybaton.kiosk_system.service.CartService;
import com.wheezybaton.kiosk_system.service.OrderService;
import com.wheezybaton.kiosk_system.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductRestController.class)
class ProductRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ProductRepository productRepo;
    @MockitoBean private CategoryRepository categoryRepo;
    @MockitoBean private IngredientRepository ingredientRepo;
    @MockitoBean private ProductIngredientRepository productIngredientRepo;
    @MockitoBean private CartService cartService;
    @MockitoBean private OrderService orderService;
    @MockitoBean private StatsService statsService;

    @Test
    @WithMockUser
    void getAllProducts_ShouldReturnPage() throws Exception {
        Product p = new Product(1L, "Test Burger", new BigDecimal("20.00"), "Desc", "img.png", null, null, false);
        Page<Product> page = new PageImpl<>(List.of(p));
        when(productRepo.findByDeletedFalse(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/products").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("Test Burger")));
    }

    @Test
    @WithMockUser
    void getProduct_ShouldReturn404_WhenNotFound() throws Exception {
        when(productRepo.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @WithMockUser
    void createProduct_ShouldReturn400_WhenValidationFails() throws Exception {
        CreateProductRequest badRequest = new CreateProductRequest();

        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createProduct_ShouldSaveAndReturnDto() throws Exception {
        // Arrange
        CreateProductRequest req = new CreateProductRequest();
        req.setName("Mega Burger");
        req.setBasePrice(new BigDecimal("30.00"));
        req.setCategoryId(1L);
        req.setIngredients(new ArrayList<>());

        CreateProductRequest.IngredientConfig ingConfig = new CreateProductRequest.IngredientConfig();
        ingConfig.setIngredientId(10L);
        ingConfig.setDefault(true);
        req.getIngredients().add(ingConfig);

        Category category = new Category(1L, "Burgers", "img.png", null);
        Ingredient ingredient = new Ingredient(10L, "Bacon", new BigDecimal("3.00"));

        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        when(ingredientRepo.findById(10L)).thenReturn(Optional.of(ingredient));

        when(productRepo.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(55L);
            return p;
        });

        Product savedProduct = new Product(55L, "Mega Burger", new BigDecimal("30.00"), null, null, category, new ArrayList<>(), false);
        ProductIngredient pi = new ProductIngredient(1L, savedProduct, ingredient, true, 1, null, 1);
        savedProduct.getProductIngredients().add(pi);

        when(productRepo.findById(55L)).thenReturn(Optional.of(savedProduct));

        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated()) // Oczekujemy 201 Created
                .andExpect(jsonPath("$.name", is("Mega Burger")))
                .andExpect(jsonPath("$.categoryName", is("Burgers")));
    }
}