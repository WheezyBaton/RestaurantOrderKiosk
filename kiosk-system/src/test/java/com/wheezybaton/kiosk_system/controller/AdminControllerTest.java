package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.repository.*;
import com.wheezybaton.kiosk_system.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
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
    @MockitoBean private CategoryRepository categoryRepo;
    @MockitoBean private IngredientRepository ingredientRepo;
    @MockitoBean private ProductIngredientRepository productIngredientRepo;
    @MockitoBean private StatsService statsService;
    @MockitoBean private com.wheezybaton.kiosk_system.service.CartService cartService;
    @MockitoBean private com.wheezybaton.kiosk_system.service.OrderService orderService;

    @BeforeEach
    void setUp() {
        when(productRepo.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void dashboard_ShouldReturnAdminView() throws Exception {
        when(productRepo.findByDeletedFalse()).thenReturn(Collections.emptyList());
        when(statsService.getSalesStats()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void showAddForm_ShouldReturnFormView() throws Exception {
        mockMvc.perform(get("/admin/products/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product-form"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteProduct_ShouldSoftDeleteAndRedirect() throws Exception {
        Product product = new Product();
        product.setId(1L);
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/admin/products/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productRepo).save(any(Product.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void saveProduct_ShouldSaveAndRedirect() throws Exception {
        MockMultipartFile file = new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "test data".getBytes());
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(new Category()));

        mockMvc.perform(multipart("/admin/products/save")
                        .file(file)
                        .param("name", "New Burger")
                        .param("basePrice", "25.00")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(productRepo).save(any(Product.class));
    }
}