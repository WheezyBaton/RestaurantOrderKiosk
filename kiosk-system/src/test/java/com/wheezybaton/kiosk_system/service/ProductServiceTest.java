package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.exception.ResourceNotFoundException;
import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.repository.CategoryRepository;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductIngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepo;
    @Mock private CategoryRepository categoryRepo;
    @Mock private IngredientRepository ingredientRepo;
    @Mock private ProductIngredientRepository productIngredientRepo;

    @InjectMocks private ProductService productService;

    @Test
    void getAllProducts_ShouldReturnActiveOnes() {
        when(productRepo.findByDeletedFalse()).thenReturn(List.of(new Product()));
        assertFalse(productService.getAllProducts().isEmpty());
    }

    @Test
    void getProductById_ShouldThrow_WhenNotFound() {
        when(productRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void createProduct_ShouldMapDtoAndSaveIngredients() {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("New Burger");
        req.setBasePrice(BigDecimal.TEN);
        req.setCategoryId(1L);
        req.setImageUrl("img.jpg");

        CreateProductRequest.IngredientConfig ingConfig = new CreateProductRequest.IngredientConfig();
        ingConfig.setIngredientId(10L);
        ingConfig.setDefault(true);
        req.setIngredients(List.of(ingConfig));

        when(categoryRepo.findById(1L)).thenReturn(Optional.of(new Category()));
        when(productRepo.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);
        when(ingredientRepo.findById(10L)).thenReturn(Optional.of(new Ingredient()));

        Product result = productService.createProduct(req);

        assertEquals("New Burger", result.getName());
        verify(productIngredientRepo).saveAll(anyList());
    }

    @Test
    void updateProduct_ShouldUpdateFieldsAndResetIngredients() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setProductIngredients(Collections.emptyList());

        CreateProductRequest req = new CreateProductRequest();
        req.setName("Updated Name");
        req.setBasePrice(BigDecimal.ONE);
        req.setCategoryId(2L);
        req.setIngredients(Collections.emptyList());

        when(productRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepo.findById(2L)).thenReturn(Optional.of(new Category()));
        when(productRepo.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        Product result = productService.updateProduct(1L, req);

        assertEquals("Updated Name", result.getName());
        verify(productIngredientRepo).deleteAll(any());
    }

    @Test
    void deleteProduct_ShouldSetFlag() {
        Product p = new Product();
        when(productRepo.findById(1L)).thenReturn(Optional.of(p));

        productService.deleteProduct(1L);

        assertTrue(p.isDeleted());
        verify(productRepo).save(p);
    }
}