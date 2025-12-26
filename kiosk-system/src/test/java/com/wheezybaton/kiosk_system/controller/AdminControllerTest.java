package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.repository.*;
import com.wheezybaton.kiosk_system.service.ProductService;
import com.wheezybaton.kiosk_system.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ProductRepository productRepo;
    @MockitoBean private ProductService productService;
    @MockitoBean private CategoryRepository categoryRepo;
    @MockitoBean private IngredientRepository ingredientRepo;
    @MockitoBean private ProductIngredientRepository productIngredientRepo;
    @MockitoBean private StatsService statsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboard_ShouldShowStats() throws Exception {
        when(productRepo.findByDeletedFalseOrderByIdAsc()).thenReturn(new ArrayList<>());
        when(statsService.getSalesStats()).thenReturn(new ArrayList<>());
        when(statsService.getTotalRevenue()).thenReturn(BigDecimal.ZERO);
        when(statsService.getMonthlyRevenue()).thenReturn(BigDecimal.ZERO);
        when(statsService.getTodayOrdersCount()).thenReturn(0L);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("products", "salesStats", "totalRevenue"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveProduct_ShouldHandleValidationErrors() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);

        mockMvc.perform(multipart("/admin/products/save")
                        .file(emptyFile)
                        .param("name", "") // Błąd
                        .param("basePrice", "10.00")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeHasFieldErrors("product", "name"));

        verify(productRepo, never()).save(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveProduct_ShouldSaveNewProductWithIngredients() throws Exception {
        Category cat = new Category();
        cat.setId(1L);
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(cat));

        Product savedProduct = new Product();
        savedProduct.setId(10L);
        when(productRepo.save(any())).thenReturn(savedProduct);

        Ingredient ing = new Ingredient();
        ing.setId(5L);
        when(ingredientRepo.findById(5L)).thenReturn(Optional.of(ing));

        MockMultipartFile file = new MockMultipartFile("imageFile", "test.png", "image/png", "test".getBytes());

        mockMvc.perform(multipart("/admin/products/save")
                        .file(file)
                        .param("name", "Burger")
                        .param("basePrice", "20.00")
                        .param("categoryId", "1")
                        .param("ingredientIds", "5")
                        .param("maxQty_5", "3")
                        .param("customPrice_5", "1.50")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productRepo).save(any(Product.class));
        verify(productIngredientRepo).saveAll(anyList());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ShouldSetDeletedFlag() throws Exception {
        Product p = new Product();
        when(productRepo.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/admin/products/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productRepo).save(p);
        assert(p.isDeleted());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadReport_ShouldReturnCsv() throws Exception {
        when(statsService.getSalesCsv()).thenReturn("col1,col2".getBytes());

        mockMvc.perform(get("/admin/report/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("attachment")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditForm_ShouldLoadProduct() throws Exception {
        Product p = new Product();
        p.setId(1L);
        when(productRepo.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/admin/products/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeExists("product", "activeIngredients"));
    }
}