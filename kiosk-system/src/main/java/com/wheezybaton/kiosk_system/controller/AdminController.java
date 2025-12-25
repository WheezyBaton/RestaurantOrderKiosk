package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.repository.CategoryRepository;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductIngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import com.wheezybaton.kiosk_system.service.ProductService;
import com.wheezybaton.kiosk_system.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductRepository productRepo;
    private final ProductService productService;
    private final CategoryRepository categoryRepo;
    private final IngredientRepository ingredientRepo;
    private final ProductIngredientRepository productIngredientRepo;
    private final StatsService statsService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("products", productRepo.findByDeletedFalse());
        model.addAttribute("salesStats", statsService.getSalesStats());
        model.addAttribute("totalRevenue", statsService.getTotalRevenue());
        model.addAttribute("todayOrders", statsService.getTodayOrdersCount());
        return "admin/dashboard";
    }

    @GetMapping("/products/toggle/{id}")
    public String toggleAvailability(@PathVariable Long id) {
        productService.toggleProductAvailability(id);
        return "redirect:/admin";
    }

    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("allIngredients", ingredientRepo.findAll());
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
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepo.findAll());
            model.addAttribute("allIngredients", ingredientRepo.findAll());
            model.addAttribute("activeIngredients", new HashMap<Long, ProductIngredient>());
            return "admin/product-form";
        }

        Category category = categoryRepo.findById(categoryId).orElseThrow();
        product.setCategory(category);

        if (product.getId() == null) {
            product.setAvailable(false);

            if (!multipartFile.isEmpty()) {
                saveImage(product, multipartFile);
            } else {
                product.setImageUrl("placeholder.png");
            }
        } else {
            Product existing = productRepo.findById(product.getId()).orElse(null);
            if (existing != null) {
                product.setAvailable(existing.isAvailable());

                if (!multipartFile.isEmpty()) {
                    saveImage(product, multipartFile);
                } else {
                    product.setImageUrl(existing.getImageUrl());
                }
            }
        }

        Product savedProduct = productRepo.save(product);

        List<ProductIngredient> currentConfigs = productIngredientRepo.findAll().stream()
                .filter(pi -> pi.getProduct().getId().equals(savedProduct.getId()))
                .collect(Collectors.toList());

        if (!currentConfigs.isEmpty()) {
            productIngredientRepo.deleteAll(currentConfigs);
            productIngredientRepo.flush();
        }

        if (ingredientIds != null) {
            List<ProductIngredient> newConfigs = new ArrayList<>();
            int displayOrder = 1;

            for (Long ingId : ingredientIds) {
                ProductIngredient config = new ProductIngredient();
                config.setProduct(savedProduct);
                config.setIngredient(ingredientRepo.findById(ingId).orElseThrow());

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
            productIngredientRepo.saveAll(newConfigs);
        }
        return "redirect:/admin";
    }

    private void saveImage(Product product, MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        Path uploadPath = Paths.get("./uploads");
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            product.setImageUrl(fileName);
        }
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        Product product = productRepo.findById(id).orElseThrow();
        product.setDeleted(true);
        productRepo.save(product);
        return "redirect:/admin";
    }

    @GetMapping("/report/export")
    public ResponseEntity<Resource> downloadReport() {
        byte[] data = statsService.getSalesCsv();
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"raport_sprzedazy.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(resource);
    }

    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono produktu o ID: " + id));

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("allIngredients", ingredientRepo.findAll());

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

        return "admin/product-form";
    }
}