package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.dto.ProductDto;
import com.wheezybaton.kiosk_system.dto.ProductIngredientDto;
import com.wheezybaton.kiosk_system.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Operation(summary = "Pobierz listę produktów (Paginacja)", description = "Zwraca stronę produktów. Parametry: page (od 0), size (domyślnie 20).")
    public ResponseEntity<Page<ProductDto>> getAllProducts(Pageable pageable) {
        Page<Product> productsPage = productRepo.findByDeletedFalse(pageable);
        Page<ProductDto> dtoPage = productsPage.map(this::mapToDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz szczegóły produktu", description = "Zwraca pełną konfigurację produktu.")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        return productRepo.findById(id)
                .filter(p -> !p.isDeleted())
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt o ID " + id + " nie istnieje"));
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
                .orElseThrow(() -> new ResourceNotFoundException("Kategoria o ID " + request.getCategoryId() + " nie istnieje"));
        product.setCategory(category);

        Product savedProduct = productRepo.save(product);

        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            List<ProductIngredient> configs = new ArrayList<>();
            int order = 1;

            for (CreateProductRequest.IngredientConfig configDto : request.getIngredients()) {
                Ingredient ingredient = ingredientRepo.findById(configDto.getIngredientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Składnik o ID " + configDto.getIngredientId() + " nie istnieje"));

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

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Zaktualizuj produkt", description = "Aktualizuje podstawowe dane produktu.")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody @Valid CreateProductRequest request) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt nie istnieje"));

        if (product.isDeleted()) {
            throw new ResourceNotFoundException("Produkt nie istnieje");
        }

        product.setName(request.getName());
        product.setBasePrice(request.getBasePrice());
        product.setDescription(request.getDescription());

        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kategoria nie istnieje"));
            product.setCategory(category);
        }

        productRepo.save(product);
        return ResponseEntity.ok(mapToDto(product));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń produkt", description = "Wykonuje miękkie usuwanie produktu (ukrycie).")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt nie istnieje"));

        product.setDeleted(true);
        productRepo.save(product);

        return ResponseEntity.noContent().build();
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