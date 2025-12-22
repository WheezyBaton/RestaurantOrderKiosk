package com.wheezybaton.kiosk_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import com.wheezybaton.kiosk_system.service.ProductService;
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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductRestController.class)
class ProductRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ProductRepository productRepo;
    @MockitoBean private ProductService productService;
    @MockitoBean private com.wheezybaton.kiosk_system.repository.CategoryRepository categoryRepo;
    @MockitoBean private com.wheezybaton.kiosk_system.repository.IngredientRepository ingredientRepo;
    @MockitoBean private com.wheezybaton.kiosk_system.repository.ProductIngredientRepository productIngredientRepo;
    @MockitoBean private com.wheezybaton.kiosk_system.service.CartService cartService;
    @MockitoBean private com.wheezybaton.kiosk_system.service.OrderService orderService;
    @MockitoBean private com.wheezybaton.kiosk_system.service.StatsService statsService;

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
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createProduct_ShouldCallServiceAndReturnDto() throws Exception {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("Service Burger");
        req.setBasePrice(BigDecimal.TEN);
        req.setCategoryId(1L);

        Product created = new Product(55L, "Service Burger", BigDecimal.TEN, null, null, null, null, false);

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(55)));
    }

    @Test
    @WithMockUser
    void deleteProduct_ShouldCallService() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }
}