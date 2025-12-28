package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.dto.OrderHistorySummaryDto;
import com.wheezybaton.kiosk_system.dto.OrderStatusHistoryDto;
import com.wheezybaton.kiosk_system.service.CategoryService;
import com.wheezybaton.kiosk_system.service.IngredientService;
import com.wheezybaton.kiosk_system.service.ProductService;
import com.wheezybaton.kiosk_system.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}