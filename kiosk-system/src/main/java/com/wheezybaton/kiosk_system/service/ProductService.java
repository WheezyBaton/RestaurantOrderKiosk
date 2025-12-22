package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.exception.ResourceNotFoundException;
import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;

    public List<Product> getAllProducts() {
        return productRepository.findByDeletedFalse();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public List<Product> getAvailableProducts() {
        return productRepository.findByDeletedFalseAndAvailableTrue();
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByDeletedFalse();
    }

    public void toggleProductAvailability(Long id) {
        Product product = getProductById(id);
        product.setAvailable(!product.isAvailable());
        productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setDeleted(true);
        productRepository.save(product);
    }

    public Product createProduct(CreateProductRequest request) {
        Product product = new Product();
        updateProductFromRequest(product, request);
        product.setAvailable(true);
        Product savedProduct = productRepository.save(product);

        saveIngredients(savedProduct, request.getIngredients());

        return savedProduct;
    }

    public Product updateProduct(Long id, CreateProductRequest request) {
        Product product = getProductById(id);
        updateProductFromRequest(product, request);
        Product savedProduct = productRepository.save(product);

        if (product.getProductIngredients() != null) {
            productIngredientRepository.deleteAll(product.getProductIngredients());
            productIngredientRepository.flush();
        }
        saveIngredients(savedProduct, request.getIngredients());

        return savedProduct;
    }

    private void updateProductFromRequest(Product product, CreateProductRequest request) {
        product.setName(request.getName());
        product.setBasePrice(request.getBasePrice());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        product.setCategory(category);
    }

    private void saveIngredients(Product product, List<CreateProductRequest.IngredientConfig> ingredientsConfig) {
        if (ingredientsConfig == null || ingredientsConfig.isEmpty()) {
            return;
        }

        List<ProductIngredient> productIngredients = new ArrayList<>();
        int order = 1;

        for (CreateProductRequest.IngredientConfig config : ingredientsConfig) {
            Ingredient ingredient = ingredientRepository.findById(config.getIngredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found id: " + config.getIngredientId()));

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