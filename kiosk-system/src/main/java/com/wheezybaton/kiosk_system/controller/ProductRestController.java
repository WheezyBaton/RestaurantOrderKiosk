package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.dto.CreateProductRequest;
import com.wheezybaton.kiosk_system.dto.ProductDto;
import com.wheezybaton.kiosk_system.dto.ProductIngredientDto;
import com.wheezybaton.kiosk_system.exception.ResourceNotFoundException;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import com.wheezybaton.kiosk_system.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Produkty", description = "Zarządzanie produktami i ich konfiguracją")
public class ProductRestController {

    private final ProductRepository productRepo;
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Pobierz listę produktów (Paginacja)", description = "Zwraca stronę produktów.")
    public ResponseEntity<Page<ProductDto>> getAllProducts(Pageable pageable) {
        Page<Product> productsPage = productRepo.findByDeletedFalse(pageable);
        return ResponseEntity.ok(productsPage.map(this::mapToDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz szczegóły produktu")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        return productRepo.findById(id)
                .filter(p -> !p.isDeleted())
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Produkt o ID " + id + " nie istnieje"));
    }

    @PostMapping
    @Operation(summary = "Utwórz produkt")
    public ResponseEntity<ProductDto> createProduct(@RequestBody @Valid CreateProductRequest request) {
        Product savedProduct = productService.createProduct(request);
        return ResponseEntity.status(201).body(mapToDto(savedProduct));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Zaktualizuj produkt")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody @Valid CreateProductRequest request) {
        Product updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(mapToDto(updatedProduct));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń produkt")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
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