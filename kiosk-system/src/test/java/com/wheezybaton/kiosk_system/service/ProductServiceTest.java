package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.exception.ResourceNotFoundException;
import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.repository.CategoryRepository;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductIngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private IngredientRepository ingredientRepository;
    @Mock private ProductIngredientRepository productIngredientRepository;

    @InjectMocks private ProductService productService;

    @Test
    void getAllProducts_ShouldReturnActiveOnes() {
        when(productRepository.findByDeletedFalse()).thenReturn(List.of(new Product()));
        assertFalse(productService.getAllProducts().isEmpty());
    }

    @Test
    void getProductById_ShouldThrow_WhenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
    }

    @Test
    void createProduct_ShouldMapDtoAndSaveIngredients() {
        CreateProductRequest.IngredientConfig ingConfig = new CreateProductRequest.IngredientConfig(10L, true, null, null);
        CreateProductRequest req = new CreateProductRequest("New Burger", BigDecimal.TEN, null, "img.jpg", 1L, List.of(ingConfig));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);
        when(ingredientRepository.findById(10L)).thenReturn(Optional.of(new Ingredient()));

        Product result = productService.createProduct(req);

        assertEquals("New Burger", result.getName());
        verify(productIngredientRepository).saveAll(anyList());
    }

    @Test
    void updateProduct_ShouldUpdateFieldsAndResetIngredients() {
        Product existing = new Product(1L, null, null, null, null, false, null, List.of(), false);

        CreateProductRequest req = new CreateProductRequest("Updated Name", BigDecimal.ONE, null, null, 2L, List.of());

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(new Category()));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        Product result = productService.updateProduct(1L, req);

        assertEquals("Updated Name", result.getName());
        verify(productIngredientRepository).deleteAll(any());
    }

    @Test
    void deleteProduct_ShouldSetFlag() {
        Product p = new Product(1L, null, null, null, null, false, null, null, false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        productService.deleteProduct(1L);

        assertTrue(p.isDeleted());
        verify(productRepository).save(p);
    }

    @Test
    void getAllProducts_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product(null, "Test Product", null, null, null, false, null, null, false);
        Page<Product> expectedPage = new PageImpl<>(List.of(product));

        when(productRepository.findByDeletedFalse(pageable)).thenReturn(expectedPage);

        Page<Product> result = productService.getAllProducts(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Product", result.getContent().get(0).getName());
        verify(productRepository).findByDeletedFalse(pageable);
    }

    @Test
    void searchProducts_ShouldFilterByNameCaseInsensitive() {
        String query = "burger";
        Product p1 = new Product(null, "Bacon Burger", null, null, null, false, null, null, false);
        Product p2 = new Product(null, "Fries", null, null, null, false, null, null, false);
        Product p3 = new Product(null, "Cheeseburger", null, null, null, false, null, null, false);

        when(productRepository.findByDeletedFalse()).thenReturn(List.of(p1, p2, p3));

        List<Product> result = productService.searchProducts(query);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Bacon Burger")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Cheeseburger")));
    }

    @Test
    void toggleProductAvailability_ShouldToggleStatusAndSave() {
        Long productId = 1L;
        Product product = new Product(productId, null, null, null, null, true, null, null, false);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.toggleProductAvailability(productId);

        assertFalse(product.isAvailable());
        verify(productRepository).save(product);
    }

    @Test
    void saveProductEntity_ShouldDelegateToRepository() {
        Product product = new Product(1L, "Legacy Admin Product", null, null, null, false, null, null, false);

        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.saveProductEntity(product);

        assertEquals("Legacy Admin Product", result.getName());
        verify(productRepository).save(product);
    }

    @Test
    void updateProductIngredients_ShouldReplaceExistingIngredients() {
        ProductIngredient oldIngredient = new ProductIngredient();
        Product product = new Product(100L, null, null, null, null, false, null, List.of(oldIngredient), false);

        List<ProductIngredient> newIngredients = List.of(new ProductIngredient(), new ProductIngredient());

        productService.updateProductIngredients(product, newIngredients);

        verify(productIngredientRepository).deleteAll(eq(List.of(oldIngredient)));
        verify(productIngredientRepository).flush();
        verify(productIngredientRepository).saveAll(newIngredients);
    }

    @Test
    void updateProductIngredients_ShouldSkipDelete_WhenNoExistingIngredients() {
        Product product = new Product(200L, null, null, null, null, false, null, List.of(), false);

        List<ProductIngredient> newIngredients = List.of(new ProductIngredient());

        productService.updateProductIngredients(product, newIngredients);

        verify(productIngredientRepository, never()).deleteAll(any());
        verify(productIngredientRepository, never()).flush();
        verify(productIngredientRepository).saveAll(newIngredients);
    }

    @Test
    void updateProductIngredients_ShouldSkipDelete_WhenProductIdIsNull() {
        Product product = new Product(null, null, null, null, null, false, null, List.of(new ProductIngredient()), false);

        List<ProductIngredient> newIngredients = List.of(new ProductIngredient());

        productService.updateProductIngredients(product, newIngredients);

        verify(productIngredientRepository, never()).deleteAll(any());
        verify(productIngredientRepository, never()).flush();
        verify(productIngredientRepository).saveAll(newIngredients);
    }

    @Test
    void createProduct_ShouldThrow_WhenCategoryNotFound() {
        CreateProductRequest request = new CreateProductRequest("Burger without Category", BigDecimal.TEN, null, null, 999L, null);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                productService.createProduct(request)
        );
        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void createProduct_ShouldThrow_WhenIngredientNotFound() {
        CreateProductRequest.IngredientConfig badConfig = new CreateProductRequest.IngredientConfig(888L, false, null, null);
        CreateProductRequest request = new CreateProductRequest("Burger with Bad Ingredient", BigDecimal.TEN, null, null, 1L, List.of(badConfig));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);
        when(ingredientRepository.findById(888L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                productService.createProduct(request)
        );
        assertTrue(exception.getMessage().contains("Ingredient not found id: 888"));
    }
}