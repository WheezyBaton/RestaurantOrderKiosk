package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.dto.OrderHistorySummaryDto;
import com.wheezybaton.kiosk_system.dto.OrderStatusHistoryDto;
import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.service.CategoryService;
import com.wheezybaton.kiosk_system.service.IngredientService;
import com.wheezybaton.kiosk_system.service.ProductService;
import com.wheezybaton.kiosk_system.service.StatsService;
import com.wheezybaton.kiosk_system.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ProductService productService;
    @MockitoBean private CategoryService categoryService;
    @MockitoBean private IngredientService ingredientService;
    @MockitoBean private StatsService statsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboard_ShouldAddGroupedHistoryToModel() throws Exception {
        OrderStatusHistoryDto step = new OrderStatusHistoryDto(1L, null, "NEW", LocalDateTime.now());
        OrderHistorySummaryDto mockSummary = new OrderHistorySummaryDto(1L, List.of(step));

        when(productService.getAllProducts()).thenReturn(List.of());
        when(statsService.getSalesStats()).thenReturn(List.of());
        when(statsService.getGroupedStatusHistory()).thenReturn(List.of(mockSummary));
        when(statsService.getTotalRevenue()).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("groupedHistory", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboard_ShouldAddAttributesToModel() throws Exception {
        OrderStatusHistoryDto step = new OrderStatusHistoryDto(1L, null, "NEW", LocalDateTime.now());
        OrderHistorySummaryDto summary = new OrderHistorySummaryDto(1L, List.of(step));

        when(productService.getAllProducts()).thenReturn(List.of());
        when(statsService.getSalesStatsGroupedByMonth()).thenReturn(Map.of());
        when(statsService.getGroupedStatusHistory()).thenReturn(List.of(summary));
        when(statsService.getTotalRevenue()).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("products", "monthlySalesStats", "groupedHistory", "totalRevenue"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleAvailability_ShouldCallServiceAndRedirect() throws Exception {
        mockMvc.perform(get("/admin/products/toggle/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
        verify(productService).toggleProductAvailability(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showAddForm_ShouldReturnFormView() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());
        when(ingredientService.getAllIngredients()).thenReturn(List.of());

        mockMvc.perform(get("/admin/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeExists("product", "categories", "allIngredients"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveProduct_WithValidationErrors_ShouldReturnForm() throws Exception {
        mockMvc.perform(multipart("/admin/products/save")
                        .file(new MockMultipartFile("imageFile", "empty.png", "image/png", new byte[0]))
                        .param("name", "Burger Bez Ceny")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeHasFieldErrors("product", "basePrice"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveProduct_SuccessfulCreate_ShouldRedirect() throws Exception {
        when(categoryService.getAllCategories())
                .thenReturn(List.of(new Category(1L, null, null, null)));

        Product savedProduct = new Product(1L, "Burger", null, null, null, true, null, null, false);

        when(productService.saveProductEntity(any(Product.class))).thenReturn(savedProduct);
        when(ingredientService.getIngredientById(anyLong())).thenReturn(new Ingredient());

        mockMvc.perform(multipart("/admin/products/save")
                        .file(new MockMultipartFile("imageFile", "test.png", "image/png", "content".getBytes()))
                        .param("name", "Burger")
                        .param("basePrice", "25.50")
                        .param("categoryId", "1")
                        .param("ingredientIds", "10", "11")
                        .param("customPrice_10", "2.00")
                        .param("maxQty_10", "5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productService).saveProductEntity(any(Product.class));
        verify(productService).updateProductIngredients(eq(savedProduct), anyList());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ShouldCallServiceAndRedirect() throws Exception {
        mockMvc.perform(get("/admin/products/delete/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
        verify(productService).deleteProduct(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadReport_ShouldReturnCsvFile() throws Exception {
        byte[] csvContent = "col1,col2\nval1,val2".getBytes();
        when(statsService.getSalesCsv()).thenReturn(csvContent);

        mockMvc.perform(get("/admin/report/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"raport_sprzedazy.csv\"")))
                .andExpect(content().contentType(MediaType.parseMediaType("text/csv")))
                .andExpect(content().bytes(csvContent));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditForm_ShouldLoadProductAndReturnView() throws Exception {
        Product product = new Product(1L, "Edit Me", null, null, null, true, null, null, false);

        when(productService.getProductById(1L)).thenReturn(product);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        when(ingredientService.getAllIngredients()).thenReturn(List.of());

        mockMvc.perform(get("/admin/products/edit/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attribute("product", hasProperty("name", is("Edit Me"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveProduct_NewProductNoImage_ShouldSetPlaceholder() throws Exception {
        when(categoryService.getAllCategories())
                .thenReturn(List.of(new Category(1L, null, null, null)));

        when(productService.saveProductEntity(any(Product.class))).thenAnswer(i -> {
            ((Product) i.getArgument(0)).setId(10L);
            return i.getArgument(0);
        });

        mockMvc.perform(multipart("/admin/products/save")
                        .file(new MockMultipartFile("imageFile", "", "image/png", new byte[0]))
                        .param("name", "New Product")
                        .param("basePrice", "10.00")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productService).saveProductEntity(captor.capture());

        assertEquals("placeholder.png", captor.getValue().getImageUrl());
        assertFalse(captor.getValue().isAvailable());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveProduct_UpdateExisting_ShouldPreserveFields_WhenNoNewImage() throws Exception {
        Product existing = new Product(5L, "Old Name", null, null, "original.png", true, null, null, false);

        when(categoryService.getAllCategories()).thenReturn(List.of(new Category(1L, null, null, null)));
        when(productService.getProductById(5L)).thenReturn(existing);
        when(productService.saveProductEntity(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(multipart("/admin/products/save")
                        .file(new MockMultipartFile("imageFile", "", "image/png", new byte[0]))
                        .param("id", "5")
                        .param("name", "New Name")
                        .param("basePrice", "20.00")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productService).saveProductEntity(captor.capture());

        assertEquals("New Name", captor.getValue().getName());
        assertTrue(captor.getValue().isAvailable());
        assertEquals("original.png", captor.getValue().getImageUrl());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveProduct_UpdateNonExistent_ShouldReturn404() throws Exception {
        when(categoryService.getAllCategories())
                .thenReturn(List.of(new Category(1L, "Test Category", null, null)));
        when(productService.getProductById(999L))
                .thenThrow(new com.wheezybaton.kiosk_system.exception.ResourceNotFoundException("Product not found"));

        mockMvc.perform(multipart("/admin/products/save")
                        .file(new MockMultipartFile("imageFile", "test.png", "image/png", "content".getBytes()))
                        .param("id", "999")
                        .param("name", "Ghost Product")
                        .param("basePrice", "10.00")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveProduct_ImageSaveError_ShouldThrowException() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(new Category(1L, null, null, null)));

        MockMultipartFile badFile = spy(new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "content".getBytes()));
        doThrow(new IOException("Disk error")).when(badFile).getInputStream();

        mockMvc.perform(multipart("/admin/products/save")
                        .file(badFile)
                        .param("name", "Product")
                        .param("basePrice", "10.00")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IOException));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditForm_WithIngredients_ShouldMapActiveIngredients() throws Exception {
        Ingredient ing1 = new Ingredient(101L, "Lettuce", null);
        Ingredient ing2 = new Ingredient(102L, "Tomato", null);

        ProductIngredient pi1 = new ProductIngredient(null, null, ing1, true, 0, null, 1);
        ProductIngredient pi2 = new ProductIngredient(null, null, ing2, false, 0, null, 1);

        Product product = new Product(1L, "Product With Ingredients", null, null, null, true, null, List.of(pi1, pi2), false);

        when(productService.getProductById(1L)).thenReturn(product);
        when(categoryService.getAllCategories()).thenReturn(List.of());
        when(ingredientService.getAllIngredients()).thenReturn(List.of());

        mockMvc.perform(get("/admin/products/edit/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(model().attribute("activeIngredients", hasEntry(101L, pi1)))
                .andExpect(model().attribute("activeIngredients", hasEntry(102L, pi2)));
    }

    @Test
    @WithMockUser(roles = "KITCHEN")
    void deleteProduct_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/admin/products/delete/1"))
                .andExpect(status().isForbidden());
    }
}