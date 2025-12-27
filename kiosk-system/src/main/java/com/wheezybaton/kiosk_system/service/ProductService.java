package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.exception.ResourceNotFoundException;
import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;

    public List<Product> getAllProducts() {
        log.debug("Retrieving all non-deleted products.");
        return productRepository.findByDeletedFalse();
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        log.debug("Retrieving all non-deleted products with pagination: {}", pageable);
        return productRepository.findByDeletedFalse(pageable);
    }

    public Product getProductById(Long id) {
        log.debug("Fetching product with ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });
    }

    public List<Product> searchProducts(String query) {
        log.debug("Searching for products with query: '{}'", query);
        return productRepository.findByDeletedFalse().stream()
                .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    public List<Product> getAvailableProducts() {
        log.debug("Retrieving all available (visible) products.");
        return productRepository.findByDeletedFalseAndAvailableTrue();
    }

    public List<Product> getAllActiveProducts() {
        log.debug("Retrieving all active products (alias for getAllProducts).");
        return productRepository.findByDeletedFalse();
    }

    @Transactional
    public void toggleProductAvailability(Long id) {
        log.debug("Toggling availability for product ID: {}", id);
        Product product = getProductById(id);
        product.setAvailable(!product.isAvailable());
        productRepository.save(product);

        log.info("Toggled availability for product ID: {}. New status: {}", id, product.isAvailable());
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.debug("Request to soft-delete product ID: {}", id);
        Product product = getProductById(id);
        product.setDeleted(true);
        productRepository.save(product);

        log.info("Soft-deleted product ID: {}, Name: {}", id, product.getName());
    }

    @Transactional
    public Product createProduct(@Valid CreateProductRequest request) {
        log.debug("Creating new product with name: {}", request.getName());

        Product product = new Product();
        updateProductFromRequest(product, request);
        product.setAvailable(true);
        Product savedProduct = productRepository.save(product);

        saveIngredients(savedProduct, request.getIngredients());

        log.info("Successfully created product. ID: {}, Name: {}", savedProduct.getId(), savedProduct.getName());
        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Long id, @Valid CreateProductRequest request) {
        log.debug("Updating product ID: {}", id);

        Product product = getProductById(id);
        updateProductFromRequest(product, request);
        Product savedProduct = productRepository.save(product);

        if (product.getProductIngredients() != null) {
            log.debug("Clearing existing ingredients for product ID: {}", id);
            productIngredientRepository.deleteAll(product.getProductIngredients());
            productIngredientRepository.flush();
        }
        saveIngredients(savedProduct, request.getIngredients());

        log.info("Successfully updated product ID: {}", id);
        return savedProduct;
    }

    @Transactional
    public Product saveProductEntity(Product product) {
        log.debug("Saving product entity directly (Admin Legacy). ID: {}", product.getId());
        return productRepository.save(product);
    }

    @Transactional
    public void updateProductIngredients(Product product, List<ProductIngredient> newIngredients) {
        log.debug("Updating ingredients for product ID: {}. New count: {}", product.getId(), newIngredients.size());

        if (product.getId() != null) {
            if (product.getProductIngredients() != null && !product.getProductIngredients().isEmpty()) {
                productIngredientRepository.deleteAll(product.getProductIngredients());
                productIngredientRepository.flush();
            }
        }

        productIngredientRepository.saveAll(newIngredients);
        log.info("Ingredients updated successfully for product ID: {}", product.getId());
    }

    private void updateProductFromRequest(Product product, CreateProductRequest request) {
        log.trace("Mapping fields from request to product entity.");
        product.setName(request.getName());
        product.setBasePrice(request.getBasePrice());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", request.getCategoryId());
                    return new ResourceNotFoundException("Category not found");
                });
        product.setCategory(category);
    }

    private void saveIngredients(Product product, List<CreateProductRequest.IngredientConfig> ingredientsConfig) {
        if (ingredientsConfig == null || ingredientsConfig.isEmpty()) {
            log.debug("No ingredients to save for product ID: {}", product.getId());
            return;
        }

        log.debug("Saving {} ingredients for product ID: {}", ingredientsConfig.size(), product.getId());
        List<ProductIngredient> productIngredients = new ArrayList<>();
        int order = 1;

        for (CreateProductRequest.IngredientConfig config : ingredientsConfig) {
            Ingredient ingredient = ingredientRepository.findById(config.getIngredientId())
                    .orElseThrow(() -> {
                        log.error("Ingredient not found with ID: {}", config.getIngredientId());
                        return new ResourceNotFoundException("Ingredient not found id: " + config.getIngredientId());
                    });

            log.trace("Adding ingredient ID: {} to product ID: {}", ingredient.getId(), product.getId());

            ProductIngredient pi = new ProductIngredient();
            pi.setProduct(product);
            pi.setIngredient(ingredient);
            pi.setDefault(config.isDefault());
            pi.setMaxQuantity(config.getMaxQuantity() != null ? config.getMaxQuantity() : 1);
            pi.setCustomPrice(config.getCustomPrice());
            pi.setDisplayOrder(order++);

            productIngredients.add(pi);
        }
        productIngredientRepository.saveAll(productIngredients);
        product.setProductIngredients(productIngredients);
    }
}