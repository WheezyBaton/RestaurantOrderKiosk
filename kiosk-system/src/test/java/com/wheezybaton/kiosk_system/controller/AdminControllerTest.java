package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.service.CategoryService;
import com.wheezybaton.kiosk_system.service.IngredientService;
import com.wheezybaton.kiosk_system.service.ProductService;
import com.wheezybaton.kiosk_system.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private IngredientService ingredientService;

    @MockitoBean
    private StatsService statsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboard_ShouldShowStats() throws Exception {
        when(statsService.getTotalRevenue()).thenReturn(BigDecimal.valueOf(1000));

        Product product = new Product();
        Category category = new Category();
        category.setName("Test Category");
        product.setCategory(category);

        when(productService.getAllProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("totalRevenue"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showAddForm_ShouldLoadData() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(new Category()));

        mockMvc.perform(get("/admin/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveProduct_ShouldSaveNewProductWithIngredients() throws Exception {
        MockMultipartFile image = new MockMultipartFile("imageFile", "test.png", "image/png", "test".getBytes());
        Category cat = new Category();
        cat.setId(1L);

        when(categoryService.getAllCategories()).thenReturn(List.of(cat));

        when(productService.saveProductEntity(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        mockMvc.perform(multipart("/admin/products/save")
                        .file(image)
                        .param("name", "Burger")
                        .param("basePrice", "25.00")
                        .param("categoryId", "1")
                        .param("ingredientIds", "10", "20")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productService).saveProductEntity(any(Product.class));
        verify(productService).updateProductIngredients(any(Product.class), anyList());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveProduct_ShouldHandleValidationErrors() throws Exception {
        MockMultipartFile image = new MockMultipartFile("imageFile", "test.png", "image/png", "test".getBytes());

        mockMvc.perform(multipart("/admin/products/save")
                        .file(image)
                        .param("name", "")
                        .param("basePrice", "25.00")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeHasFieldErrors("product", "name"));

        verify(productService, never()).saveProductEntity(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ShouldSetDeletedFlag() throws Exception {
        mockMvc.perform(get("/admin/products/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productService).deleteProduct(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadReport_ShouldReturnCsv() throws Exception {
        when(statsService.getSalesCsv()).thenReturn("col1,col2\nval1,val2".getBytes());

        mockMvc.perform(get("/admin/report/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("text/csv")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"raport_sprzedazy.csv\""));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditForm_ShouldLoadProduct() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setName("Burger");

        when(productService.getProductById(1L)).thenReturn(p);

        mockMvc.perform(get("/admin/products/edit/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("product", p));
    }
}