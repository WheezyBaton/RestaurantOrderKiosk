package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.service.CategoryService;
import com.wheezybaton.kiosk_system.service.IngredientService;
import com.wheezybaton.kiosk_system.service.ProductService;
import com.wheezybaton.kiosk_system.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final IngredientService ingredientService;
    private final StatsService statsService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("monthlySalesStats", statsService.getSalesStatsGroupedByMonth());
        model.addAttribute("groupedHistory", statsService.getGroupedStatusHistory());
        model.addAttribute("totalRevenue", statsService.getTotalRevenue());
        model.addAttribute("monthlyRevenue", statsService.getMonthlyRevenue());
        model.addAttribute("monthlyOrders", statsService.getMonthlyOrdersCount());
        model.addAttribute("todayOrders", statsService.getTodayOrdersCount());
        model.addAttribute("todayRevenue", statsService.getTodayRevenue());

        return "admin/dashboard";
    }

    @GetMapping("/products/toggle/{id}")
    public String toggleAvailability(@PathVariable Long id) {
        log.debug("Request to toggle availability for product ID: {}", id);
        productService.toggleProductAvailability(id);
        log.info("Availability toggled for product ID: {}", id);
        return "redirect:/admin";
    }

    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("allIngredients", ingredientService.getAllIngredients());
        model.addAttribute("activeIngredients", new HashMap<Long, ProductIngredient>());

        return "admin/product-form";
    }

    @PostMapping("/products/save")
    @Transactional
    public String saveProduct(
            @Valid @ModelAttribute Product product,
            BindingResult bindingResult,
            @RequestParam("imageFile") MultipartFile multipartFile,
            @RequestParam Long categoryId,
            @RequestParam(required = false) List<Long> ingredientIds,
            @RequestParam(required = false) List<Long> defaultIngredientIds,
            HttpServletRequest request,
            Model model
    ) throws IOException {
        log.debug("Attempting to save product: {}", product.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for product save request: {}", bindingResult.getAllErrors());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("allIngredients", ingredientService.getAllIngredients());
            model.addAttribute("activeIngredients", new HashMap<Long, ProductIngredient>());
            return "admin/product-form";
        }

        Category category = categoryService.getAllCategories().stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);

        if (product.getId() == null) {
            log.debug("Creating new product entity...");
            product.setAvailable(false);
            if (!multipartFile.isEmpty()) {
                saveImage(product, multipartFile);
            } else {
                product.setImageUrl("placeholder.png");
            }
        } else {
            log.debug("Updating existing product entity ID: {}", product.getId());
            Product existing = productService.getProductById(product.getId());
            product.setAvailable(existing.isAvailable());

            if (!multipartFile.isEmpty()) {
                saveImage(product, multipartFile);
            } else {
                product.setImageUrl(existing.getImageUrl());
            }
        }

        Product savedProduct = productService.saveProductEntity(product);
        log.info("Product entity saved. ID: {}, Name: {}", savedProduct.getId(), savedProduct.getName());

        if (ingredientIds != null) {
            log.debug("Processing {} new ingredient configurations...", ingredientIds.size());
            List<ProductIngredient> newConfigs = new ArrayList<>();
            int displayOrder = 1;

            for (Long ingId : ingredientIds) {
                ProductIngredient config = new ProductIngredient();
                config.setProduct(savedProduct);
                config.setIngredient(ingredientService.getIngredientById(ingId));

                boolean isDefault = defaultIngredientIds != null && defaultIngredientIds.contains(ingId);
                config.setDefault(isDefault);
                config.setDisplayOrder(displayOrder++);

                String priceParam = request.getParameter("customPrice_" + ingId);
                String maxParam = request.getParameter("maxQty_" + ingId);

                if (priceParam != null && !priceParam.isEmpty()) {
                    config.setCustomPrice(new BigDecimal(priceParam));
                }
                if (maxParam != null && !maxParam.isEmpty()) {
                    config.setMaxQuantity(Integer.parseInt(maxParam));
                } else {
                    config.setMaxQuantity(1);
                }

                newConfigs.add(config);
            }

            productService.updateProductIngredients(savedProduct, newConfigs);

            log.info("Ingredients updated for product ID: {}", savedProduct.getId());
        }
        return "redirect:/admin";
    }

    private void saveImage(Product product, MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        log.debug("Processing image upload: {}", fileName);

        Path uploadPath = Paths.get("./uploads");
        if (!Files.exists(uploadPath)) {
            log.debug("Creating upload directory: {}", uploadPath.toAbsolutePath());
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            product.setImageUrl(fileName);
            log.info("Image successfully saved: {}", fileName);
        } catch (IOException e) {
            log.error("Failed to save image file: {}", fileName, e);
            throw e;
        }
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        log.debug("Request to delete product ID: {}", id);
        productService.deleteProduct(id);
        return "redirect:/admin";
    }

    @GetMapping("/report/export")
    public ResponseEntity<Resource> downloadReport() {
        log.info("Request received to export sales report (CSV).");

        byte[] data = statsService.getSalesCsv();
        ByteArrayResource resource = new ByteArrayResource(data);

        log.debug("Sales report generated. Size: {} bytes", data.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"raport_sprzedazy.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.debug("Loading edit form for product ID: {}", id);

        Product product = productService.getProductById(id);

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("allIngredients", ingredientService.getAllIngredients());

        Map<Long, ProductIngredient> activeIngredients = new HashMap<>();
        if (product.getProductIngredients() != null) {
            activeIngredients = product.getProductIngredients().stream()
                    .collect(Collectors.toMap(
                            pi -> pi.getIngredient().getId(),
                            pi -> pi,
                            (existing, replacement) -> existing
                    ));
        }
        model.addAttribute("activeIngredients", activeIngredients);

        log.debug("Edit form loaded for product: {}", product.getName());
        return "admin/product-form";
    }
}