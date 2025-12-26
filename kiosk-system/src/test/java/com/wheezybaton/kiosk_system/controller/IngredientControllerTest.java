package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IngredientController.class)
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngredientRepository ingredientRepo;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListIngredients() throws Exception {
        when(ingredientRepo.findAll()).thenReturn(List.of(new Ingredient()));

        mockMvc.perform(get("/admin/ingredients"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/ingredients"))
                .andExpect(model().attributeExists("ingredients"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSaveIngredient() throws Exception {
        mockMvc.perform(post("/admin/ingredients/save")
                        .with(csrf())
                        .param("name", "Bacon")
                        .param("price", "3.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ingredients"));

        verify(ingredientRepo).save(any(Ingredient.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteIngredient() throws Exception {
        mockMvc.perform(get("/admin/ingredients/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ingredients"));

        verify(ingredientRepo).deleteById(1L);
    }
}