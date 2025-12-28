package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.service.IngredientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IngredientController.class)
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngredientService ingredientService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListIngredients() throws Exception {
        when(ingredientService.getAllIngredients()).thenReturn(List.of(new Ingredient()));

        mockMvc.perform(get("/admin/ingredients"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/ingredients"))
                .andExpect(model().attributeExists("ingredients"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSaveIngredient() throws Exception {
        when(ingredientService.saveIngredient(any(Ingredient.class))).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(post("/admin/ingredients/save")
                        .with(csrf())
                        .param("name", "Bacon")
                        .param("price", "3.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ingredients"));

        verify(ingredientService).saveIngredient(any(Ingredient.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteIngredient() throws Exception {
        mockMvc.perform(get("/admin/ingredients/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ingredients"));

        verify(ingredientService).deleteIngredient(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showAddForm_ShouldReturnViewWithEmptyModel() throws Exception {
        mockMvc.perform(get("/admin/ingredients/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/ingredient-form"))
                .andExpect(model().attributeExists("ingredient"))
                .andExpect(model().attribute("ingredient", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditForm_ShouldReturnView_WhenIngredientExists() throws Exception {
        Long ingredientId = 1L;
        Ingredient ingredient = new Ingredient();
        ingredient.setId(ingredientId);
        ingredient.setName("Tomato");

        when(ingredientService.getIngredientById(ingredientId)).thenReturn(ingredient);

        mockMvc.perform(get("/admin/ingredients/edit/" + ingredientId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/ingredient-form"))
                .andExpect(model().attribute("ingredient", ingredient));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void showEditForm_ShouldThrowException_WhenIngredientNotFound() throws Exception {
        Long nonExistentId = 99L;
        when(ingredientService.getIngredientById(nonExistentId)).thenReturn(null);

        mockMvc.perform(get("/admin/ingredients/edit/" + nonExistentId))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResolvedException() instanceof RuntimeException))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertEquals(
                        "Ingredient not found", result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteIngredient_ShouldRedirectWithError_WhenExceptionOccurs() throws Exception {
        Long ingredientId = 5L;
        doThrow(new RuntimeException("Data integrity violation"))
                .when(ingredientService).deleteIngredient(ingredientId);

        mockMvc.perform(get("/admin/ingredients/delete/" + ingredientId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ingredients?error=used"));

        verify(ingredientService).deleteIngredient(ingredientId);
    }
}