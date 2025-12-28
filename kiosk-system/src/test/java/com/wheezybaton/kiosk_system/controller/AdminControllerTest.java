package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.dto.OrderHistorySummaryDto;
import com.wheezybaton.kiosk_system.dto.OrderStatusHistoryDto;
import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.service.CategoryService;
import com.wheezybaton.kiosk_system.service.IngredientService;
import com.wheezybaton.kiosk_system.service.ProductService;
import com.wheezybaton.kiosk_system.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void dashboard_ShouldAddGroupedHistoryToModel() throws Exception {
        OrderStatusHistoryDto step = new OrderStatusHistoryDto(1L, null, "NEW", LocalDateTime.now());

        OrderHistorySummaryDto mockSummary = new OrderHistorySummaryDto(1L, List.of(step));

        when(productService.getAllProducts()).thenReturn(Collections.emptyList());
        when(statsService.getSalesStats()).thenReturn(Collections.emptyList());
        when(statsService.getGroupedStatusHistory()).thenReturn(List.of(mockSummary));

        when(statsService.getTotalRevenue()).thenReturn(BigDecimal.ZERO);
        when(statsService.getMonthlyRevenue()).thenReturn(BigDecimal.ZERO);
        when(statsService.getTodayRevenue()).thenReturn(BigDecimal.ZERO);
        when(statsService.getMonthlyOrdersCount()).thenReturn(0L);
        when(statsService.getTodayOrdersCount()).thenReturn(0L);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("groupedHistory"))
                .andExpect(model().attribute("groupedHistory", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void dashboard_ShouldAddAttributesToModel() throws Exception {
        OrderStatusHistoryDto step = new OrderStatusHistoryDto(1L, null, "NEW", LocalDateTime.now());
        OrderHistorySummaryDto mockSummary = new OrderHistorySummaryDto(1L, List.of(step));

        when(productService.getAllProducts()).thenReturn(Collections.emptyList());
        when(statsService.getSalesStatsGroupedByMonth()).thenReturn(Collections.emptyMap());
        when(statsService.getGroupedStatusHistory()).thenReturn(List.of(mockSummary));
        when(statsService.getTotalRevenue()).thenReturn(BigDecimal.ZERO);
        when(statsService.getMonthlyRevenue()).thenReturn(BigDecimal.ZERO);
        when(statsService.getTodayRevenue()).thenReturn(BigDecimal.ZERO);
        when(statsService.getMonthlyOrdersCount()).thenReturn(0L);
        when(statsService.getTodayOrdersCount()).thenReturn(0L);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("products", "monthlySalesStats", "groupedHistory", "totalRevenue"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void toggleAvailability_ShouldCallServiceAndRedirect() throws Exception {
        mockMvc.perform(get("/admin/products/toggle/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productService).toggleProductAvailability(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void showAddForm_ShouldReturnFormView() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());
        when(ingredientService.getAllIngredients()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeExists("product", "categories", "allIngredients"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void saveProduct_WithValidationErrors_ShouldReturnForm() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("imageFile", "empty.png", "image/png", new byte[0]);
        mockMvc.perform(multipart("/admin/products/save")
                        .file(emptyFile)
                        .param("name", "Burger Bez Ceny")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeHasFieldErrors("product", "basePrice"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void saveProduct_SuccessfulCreate_ShouldRedirect() throws Exception {
        Category category = new Category();
        category.setId(1L);
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Burger");
        when(productService.saveProductEntity(any(Product.class))).thenReturn(savedProduct);
        when(ingredientService.getIngredientById(anyLong())).thenReturn(new Ingredient());

        MockMultipartFile file = new MockMultipartFile("imageFile", "test.png", "image/png", "content".getBytes());

        mockMvc.perform(multipart("/admin/products/save")
                        .file(file)
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteProduct_ShouldCallServiceAndRedirect() throws Exception {
        mockMvc.perform(get("/admin/products/delete/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productService).deleteProduct(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void showEditForm_ShouldLoadProductAndReturnView() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Edit Me");

        when(productService.getProductById(1L)).thenReturn(product);
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());
        when(ingredientService.getAllIngredients()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/products/edit/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attribute("product", hasProperty("name", is("Edit Me"))));
    }
}