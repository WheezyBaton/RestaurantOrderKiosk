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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    @WithMockUser
    void getAllProducts_ShouldReturnPage() throws Exception {
        Product p = new Product();
        p.setName("Test Burger");
        Page<Product> page = new PageImpl<>(List.of(p));

        when(productRepo.findByDeletedFalse(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Test Burger")));
    }

    @Test
    @WithMockUser
    void createProduct_ShouldCallServiceAndReturnDto() throws Exception {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("Burger");
        req.setBasePrice(BigDecimal.TEN);
        req.setCategoryId(1L);

        Product saved = new Product();
        saved.setId(1L);
        saved.setName("Burger");
        saved.setBasePrice(BigDecimal.TEN);

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Burger")));
    }

    @Test
    @WithMockUser
    void searchProducts_ShouldFilterByName() throws Exception {
        Product p1 = new Product();
        p1.setName("Mega Burger");
        Product p2 = new Product();
        p2.setName("Fries");

        when(productRepo.findByDeletedFalse()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/v1/products/search").param("query", "Burger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Mega Burger")));
    }

    @Test
    @WithMockUser
    void updateProduct_ShouldCallService() throws Exception {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("Updated");
        req.setBasePrice(BigDecimal.TEN);
        req.setCategoryId(1L);

        Product updated = new Product();
        updated.setId(1L);
        updated.setName("Updated");

        when(productService.updateProduct(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/products/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated")));
    }
}