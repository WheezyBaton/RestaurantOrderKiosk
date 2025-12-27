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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Produkty", description = "Zarządzanie produktami i ich konfiguracją")
public class ProductRestController {

    private final ProductRepository productRepo;
    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Pobierz listę produktów (Paginacja)")
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {

        log.debug("REST request to retrieve all products (paginated). Pageable: {}", pageable);

        Page<Product> productsPage = productRepo.findByDeletedFalse(pageable);

        log.debug("Retrieved {} products for the current page.", productsPage.getNumberOfElements());
        return ResponseEntity.ok(productsPage.map(this::mapToDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz szczegóły produktu")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        log.debug("REST request to retrieve product details for ID: {}", id);

        return productRepo.findById(id)
                .filter(p -> !p.isDeleted())
                .map(this::mapToDto)
                .map(dto -> {
                    log.debug("Product found: {}", dto.getName());
                    return ResponseEntity.ok(dto);
                })
                .orElseThrow(() -> {
                    log.error("Product with ID {} not found or is deleted.", id);
                    return new ResourceNotFoundException("Produkt o ID " + id + " nie istnieje");
                });
    }

    @PostMapping
    @Operation(summary = "Utwórz produkt")
    public ResponseEntity<ProductDto> createProduct(@RequestBody @Valid CreateProductRequest request) {
        log.debug("REST request to create a new product: {}", request.getName());

        Product savedProduct = productService.createProduct(request);

        log.info("Product created successfully via API. ID: {}, Name: {}", savedProduct.getId(), savedProduct.getName());
        return ResponseEntity.status(201).body(mapToDto(savedProduct));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Zaktualizuj produkt")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody @Valid CreateProductRequest request) {
        log.debug("REST request to update product with ID: {}", id);

        Product updatedProduct = productService.updateProduct(id, request);

        log.info("Product updated successfully via API. ID: {}", updatedProduct.getId());
        return ResponseEntity.ok(mapToDto(updatedProduct));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń produkt")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.debug("REST request to delete product with ID: {}", id);

        productService.deleteProduct(id);

        log.info("Product with ID: {} deleted via API.", id);
        return ResponseEntity.noContent().build();
    }

    private ProductDto mapToDto(Product product) {
        log.trace("Mapping product entity (ID: {}) to DTO.", product.getId());

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

    @GetMapping("/search")
    @Operation(summary = "Wyszukaj produkty po nazwie", description = "Zwraca listę produktów pasujących do podanej nazwy.")
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam String query) {
        log.debug("REST request to search products with query: '{}'", query);

        List<Product> products = productRepo.findByDeletedFalse().stream()
                .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        log.debug("Found {} products matching query: '{}'", products.size(), query);
        return ResponseEntity.ok(products.stream().map(this::mapToDto).collect(Collectors.toList()));
    }
}