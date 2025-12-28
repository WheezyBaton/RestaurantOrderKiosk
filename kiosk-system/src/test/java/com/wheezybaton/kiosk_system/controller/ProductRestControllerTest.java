package com.wheezybaton.kiosk_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
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

    @Test
    @WithMockUser
    void getProduct_ShouldThrow404_WhenProductIsDeleted() throws Exception {
        Product deletedProduct = new Product();
        deletedProduct.setId(99L);
        deletedProduct.setDeleted(true);

        when(productService.getProductById(99L)).thenReturn(deletedProduct);

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResolvedException() instanceof com.wheezybaton.kiosk_system.exception.ResourceNotFoundException));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_ShouldReturnUpdatedDetails() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Updated Burger");
        request.setBasePrice(BigDecimal.valueOf(25));
        request.setCategoryId(1L);

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Burger");
        updatedProduct.setBasePrice(BigDecimal.valueOf(25));

        when(productService.updateProduct(any(Long.class), any(CreateProductRequest.class)))
                .thenReturn(updatedProduct);

        mockMvc.perform(put("/api/v1/products/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Burger"))
                .andExpect(jsonPath("$.basePrice").value(25));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ShouldReturnNoContent() throws Exception {

        mockMvc.perform(delete("/api/v1/products/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        org.mockito.Mockito.verify(productService).deleteProduct(1L);
    }

    @Test
    @WithMockUser
    void getProduct_ShouldMapCategoryAndIngredients() throws Exception {
        Product product = new Product();
        product.setId(2L);
        product.setName("Cheeseburger");
        product.setBasePrice(BigDecimal.valueOf(15));

        Category category = new com.wheezybaton.kiosk_system.model.Category();
        category.setName("Burgers");
        product.setCategory(category);

        Ingredient ing = new com.wheezybaton.kiosk_system.model.Ingredient();
        ing.setId(10L);
        ing.setName("Cheese");
        ing.setPrice(BigDecimal.ONE);

        ProductIngredient pi = new com.wheezybaton.kiosk_system.model.ProductIngredient();
        pi.setIngredient(ing);
        pi.setCustomPrice(BigDecimal.valueOf(2));

        pi.setDefault(true);
        pi.setMaxQuantity(5);

        product.setProductIngredients(List.of(pi));

        when(productService.getProductById(2L)).thenReturn(product);

        mockMvc.perform(get("/api/v1/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Burgers"))
                .andExpect(jsonPath("$.ingredients[0].ingredientId").value(10))
                .andExpect(jsonPath("$.ingredients[0].name").value("Cheese"))
                .andExpect(jsonPath("$.ingredients[0].price").value(2))
                .andExpect(jsonPath("$.ingredients[0].default").value(true))
                .andExpect(jsonPath("$.ingredients[0].maxQuantity").value(5));
    }
}