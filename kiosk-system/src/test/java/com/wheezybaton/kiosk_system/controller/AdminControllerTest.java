package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.repository.*;
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
import static org.mockito.Mockito.*;
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

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSaveProduct_HappyPath() throws Exception {
        MockMultipartFile file = new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "content".getBytes());
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(new com.wheezybaton.kiosk_system.model.Category()));
        when(productRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(multipart("/admin/products/save")
                        .file(file)
                        .param("name", "Burger")
                        .param("basePrice", "20.00")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnForm_WhenValidationFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile("imageFile", "", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/admin/products/save")
                        .file(file)
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().isOk()) // Nie przekierowanie, tylko powrót do widoku
                .andExpect(view().name("admin/product-form"))
                .andExpect(model().attributeHasFieldErrors("product", "name", "basePrice"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDelete() throws Exception {
        Product p = new Product();
        when(productRepo.findById(1L)).thenReturn(Optional.of(p));
        mockMvc.perform(get("/admin/products/delete/1")).andExpect(status().is3xxRedirection());
        verify(productRepo).save(p);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExportCsv() throws Exception {
        when(statsService.getSalesCsv()).thenReturn("col1,col2".getBytes());
        mockMvc.perform(get("/admin/report/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }
}