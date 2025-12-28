package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.OrderHistorySummaryDto;
import com.wheezybaton.kiosk_system.dto.OrderStatusHistoryDto;
import com.wheezybaton.kiosk_system.dto.SalesStatDto;
import com.wheezybaton.kiosk_system.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StatsService statsService;

    @Test
    void logStatusChange_ShouldExecuteUpdate() {
        statsService.logStatusChange(1L, OrderStatus.NEW, OrderStatus.IN_PROGRESS);

        verify(jdbcTemplate).update(
                contains("INSERT INTO order_status_history"),
                eq(1L),
                eq("NEW"),
                eq("IN_PROGRESS"),
                any(LocalDateTime.class)
        );
    }

    @Test
    void getSalesStats_ShouldReturnList() {
        SalesStatDto mockStat = new SalesStatDto("Burger", 5L, BigDecimal.valueOf(100));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of(mockStat));

        List<SalesStatDto> result = statsService.getSalesStats();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("Burger");
    }

    @Test
    void getGroupedStatusHistory_ShouldGroupAndSortCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        OrderStatusHistoryDto log1_Order1 = new OrderStatusHistoryDto(1L, null, "NEW", now.minusMinutes(10));
        OrderStatusHistoryDto log2_Order1 = new OrderStatusHistoryDto(1L, "NEW", "IN_PROGRESS", now.minusMinutes(5));

        OrderStatusHistoryDto log1_Order2 = new OrderStatusHistoryDto(2L, null, "NEW", now.minusMinutes(2));

        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of(log1_Order1, log2_Order1, log1_Order2));

        List<OrderHistorySummaryDto> result = statsService.getGroupedStatusHistory();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderId()).isEqualTo(2L);
        assertThat(result.get(1).getOrderId()).isEqualTo(1L);

        OrderHistorySummaryDto summaryOrder1 = result.get(1);
        assertThat(summaryOrder1.getHistorySteps()).hasSize(2);
        assertThat(summaryOrder1.getHistorySteps().get(0).getNewStatus()).isEqualTo("NEW");
        assertThat(summaryOrder1.getHistorySteps().get(1).getNewStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void getGroupedStatusHistory_ShouldHandleEmptyList() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(Collections.emptyList());

        List<OrderHistorySummaryDto> result = statsService.getGroupedStatusHistory();

        assertThat(result).isEmpty();
    }
}