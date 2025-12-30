package com.wheezybaton.kiosk_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.service.ProductService;
import com.wheezybaton.kiosk_system.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductRestController.class)
@Import(SecurityConfig.class)
class ProductRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ProductService productService;

    @Test
    @WithMockUser
    void getAllProducts_ShouldReturnPage() throws Exception {
        Product p = new Product(1L, "Burger", BigDecimal.TEN, null, null, true, null, null, false);

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
        Product p = new Product(1L, "Fries", BigDecimal.valueOf(5), null, null, true, null, null, false);

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

        Product saved = new Product(10L, "New Burger", BigDecimal.valueOf(20), null, null, true, null, null, false);

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
        Product p = new Product(1L, "Cheese Burger", BigDecimal.TEN, null, null, true, null, null, false);

        when(productService.searchProducts("Burger")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/products/search")
                        .param("query", "Burger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cheese Burger"));
    }

    @Test
    @WithMockUser
    void getProduct_ShouldThrow404_WhenProductIsDeleted() throws Exception {
        Product deletedProduct = new Product(99L, null, null, null, null, false, null, null, true);

        when(productService.getProductById(99L)).thenReturn(deletedProduct);

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof com.wheezybaton.kiosk_system.exception.ResourceNotFoundException));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_ShouldReturnUpdatedDetails() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Updated Burger");
        request.setBasePrice(BigDecimal.valueOf(25));
        request.setCategoryId(1L);

        Product updatedProduct = new Product(1L, "Updated Burger", BigDecimal.valueOf(25), null, null, true, null, null, false);

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

        verify(productService).deleteProduct(1L);
    }

    @Test
    @WithMockUser
    void getProduct_ShouldMapCategoryAndIngredients() throws Exception {
        Category category = new Category(null, "Burgers", null, null);
        Ingredient ing = new Ingredient(10L, "Cheese", BigDecimal.ONE);

        ProductIngredient pi = new ProductIngredient(null, null, ing, true, 0, BigDecimal.valueOf(2), 5);

        Product product = new Product(2L, "Cheeseburger", BigDecimal.valueOf(15), null, null, true, category, List.of(pi), false);

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

    @Test
    @WithMockUser(roles = "KITCHEN")
    void deleteProduct_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(productService, never()).deleteProduct(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("");
        request.setBasePrice(BigDecimal.valueOf(-10));

        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any());
    }
}