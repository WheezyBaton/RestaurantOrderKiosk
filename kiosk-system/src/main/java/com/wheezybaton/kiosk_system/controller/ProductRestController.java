package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.dto.ProductDto;
import com.wheezybaton.kiosk_system.dto.ProductIngredientDto;
import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.repository.CategoryRepository;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductIngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Produkty", description = "Zarządzanie produktami i ich konfiguracją")
public class ProductRestController {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final IngredientRepository ingredientRepo;
    private final ProductIngredientRepository productIngredientRepo;

    @GetMapping
    @Operation(summary = "Pobierz listę produktów", description = "Zwraca produkty wraz z ich możliwymi dodatkami.")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productRepo.findByDeletedFalse();
        List<ProductDto> dtos = products.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz szczegóły produktu", description = "Zwraca pełną konfigurację produktu.")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        return productRepo.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Utwórz produkt z konfiguracją", description = "Tworzy produkt, przypisuje kategorię i definiuje listę dostępnych składników.")
    public ResponseEntity<ProductDto> createProduct(@RequestBody @Valid CreateProductRequest request) {

        Product product = new Product();
        product.setName(request.getName());
        product.setBasePrice(request.getBasePrice());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl() != null ? request.getImageUrl() : "placeholder.png");

        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);

        Product savedProduct = productRepo.save(product);

        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            List<ProductIngredient> configs = new ArrayList<>();
            int order = 1;

            for (CreateProductRequest.IngredientConfig configDto : request.getIngredients()) {
                Ingredient ingredient = ingredientRepo.findById(configDto.getIngredientId())
                        .orElseThrow(() -> new RuntimeException("Ingredient not found ID: " + configDto.getIngredientId()));

                ProductIngredient pi = new ProductIngredient();
                pi.setProduct(savedProduct);
                pi.setIngredient(ingredient);
                pi.setDefault(configDto.isDefault());
                pi.setMaxQuantity(configDto.getMaxQuantity() != null ? configDto.getMaxQuantity() : 1);
                pi.setCustomPrice(configDto.getCustomPrice());
                pi.setDisplayOrder(order++);

                configs.add(pi);
            }
            productIngredientRepo.saveAll(configs);
            savedProduct = productRepo.findById(savedProduct.getId()).orElseThrow();
        }
        return ResponseEntity.status(201).body(mapToDto(savedProduct));
    }

    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBasePrice(product.getBasePrice());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());

        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getProductIngredients() != null) {
            List<ProductIngredientDto> ingredientDtos = product.getProductIngredients().stream()
                    .map(pi -> {
                        ProductIngredientDto pid = new ProductIngredientDto();
                        pid.setIngredientId(pi.getIngredient().getId());
                        pid.setName(pi.getIngredient().getName());
                        pid.setPrice(pi.getEffectivePrice());
                        pid.setDefault(pi.isDefault());
                        pid.setMaxQuantity(pi.getMaxQuantity());
                        return pid;
                    })
                    .collect(Collectors.toList());
            dto.setIngredients(ingredientDtos);
        }
        return dto;
    }
}