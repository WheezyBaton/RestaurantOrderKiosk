package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.repository.CategoryRepository;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductIngredientRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import com.wheezybaton.kiosk_system.service.CartService;
import com.wheezybaton.kiosk_system.service.OrderService;
import com.wheezybaton.kiosk_system.service.ProductService;
import com.wheezybaton.kiosk_system.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    @MockitoBean private CartService cartService;
    @MockitoBean private OrderService orderService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowAddForm() throws Exception {
        mockMvc.perform(get("/admin/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSaveProduct_HappyPath() throws Exception {
        Category cat = new Category();
        cat.setId(1L);
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(cat));
        when(productRepo.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        MockMultipartFile file = new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/admin/products/save")
                        .file(file)
                        .param("name", "New Burger")
                        .param("basePrice", "20.00")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnForm_WhenValidationFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile("imageFile", "", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/admin/products/save")
                        .file(file)
                        .param("name", "")
                        .param("basePrice", "20.00")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDelete() throws Exception {
        Product p = new Product();
        p.setId(10L);
        when(productRepo.findById(10L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/admin/products/delete/10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productRepo).save(p);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExportCsv() throws Exception {
        when(statsService.getSalesCsv()).thenReturn("col1,col2\nval1,val2".getBytes());

        mockMvc.perform(get("/admin/report/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"raport_sprzedazy.csv\""))
                .andExpect(content().contentType("text/csv"));
    }
}