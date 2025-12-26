package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Order;
import com.wheezybaton.kiosk_system.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KitchenController.class)
class KitchenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    @WithMockUser(roles = "KITCHEN")
    void shouldShowKitchenPanel() throws Exception {
        when(orderService.getOrdersInProgress()).thenReturn(List.of(new Order()));
        when(orderService.getOrdersReady()).thenReturn(List.of());

        mockMvc.perform(get("/kitchen"))
                .andExpect(status().isOk())
                .andExpect(view().name("kitchen"))
                .andExpect(model().attributeExists("inProgress"))
                .andExpect(model().attributeExists("ready"));
    }

    @Test
    @WithMockUser(roles = "KITCHEN")
    void shouldPromoteOrder() throws Exception {
        mockMvc.perform(post("/kitchen/promote/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/kitchen"));

        verify(orderService).promoteOrderStatus(1L);
    }
}