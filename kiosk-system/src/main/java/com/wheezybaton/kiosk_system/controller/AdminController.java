package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.repository.CategoryRepository;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductIngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import com.wheezybaton.kiosk_system.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final IngredientRepository ingredientRepo;
    private final ProductIngredientRepository productIngredientRepo;
    private final StatsService statsService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("products", productRepo.findAll());
        model.addAttribute("salesStats", statsService.getSalesStats());
        model.addAttribute("totalRevenue", statsService.getTotalRevenue());
        model.addAttribute("todayOrders", statsService.getTodayOrdersCount());
        return "admin/dashboard";
    }

    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("allIngredients", ingredientRepo.findAll());
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    @Transactional
    public String saveProduct(
            @ModelAttribute Product product,
            @RequestParam("imageFile") MultipartFile multipartFile,
            @RequestParam Long categoryId,
            @RequestParam(required = false) List<Long> ingredientIds,
            @RequestParam(required = false) List<Long> defaultIngredientIds,
            HttpServletRequest request
    ) throws IOException {
        Category category = categoryRepo.findById(categoryId).orElseThrow();
        product.setCategory(category);

        if (!multipartFile.isEmpty()) {
            String fileName = multipartFile.getOriginalFilename();
            Path uploadPath = Paths.get("./uploads");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                product.setImageUrl(fileName);
            }
        } else if (product.getId() == null) {
            product.setImageUrl("placeholder.png");
        } else {
            Product existing = productRepo.findById(product.getId()).orElse(null);
            if (existing != null) product.setImageUrl(existing.getImageUrl());
        }
        Product savedProduct = productRepo.save(product);

        List<ProductIngredient> currentConfigs = savedProduct.getProductIngredients();
        if (currentConfigs != null && !currentConfigs.isEmpty()) {
            productIngredientRepo.deleteAll(currentConfigs);
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

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productRepo.deleteById(id);
        return "redirect:/admin";
    }
}