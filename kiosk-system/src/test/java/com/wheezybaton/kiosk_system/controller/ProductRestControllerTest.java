package com.wheezybaton.kiosk_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductRestController.class)
class ProductRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getAllProducts_ShouldReturnPage() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setName("Burger");
        p.setBasePrice(BigDecimal.TEN);

        when(productService.getAllProducts(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p)));

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Burger"));
    }

    @Test
    @WithMockUser
    void getProduct_ShouldReturnDetails() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setName("Fries");
        p.setBasePrice(BigDecimal.valueOf(5));

        when(productService.getProductById(1L)).thenReturn(p);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fries"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_ShouldReturnCreated() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("New Burger");
        request.setBasePrice(BigDecimal.valueOf(20));
        request.setCategoryId(1L);

        Product saved = new Product();
        saved.setId(10L);
        saved.setName("New Burger");
        saved.setBasePrice(BigDecimal.valueOf(20));

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Burger"));
    }

    @Test
    @WithMockUser
    void searchProducts_ShouldFilterByName() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setName("Cheese Burger");
        p.setBasePrice(BigDecimal.TEN);

        when(productService.searchProducts("Burger")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/products/search")
                        .param("query", "Burger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cheese Burger"));
    }
}