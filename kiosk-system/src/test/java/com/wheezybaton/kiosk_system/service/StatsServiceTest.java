package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.SalesStatDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StatsService statsService;

    @Test
    void getTotalRevenue_ShouldReturnAmount() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Double.class))).thenReturn(100.50);
        Double result = statsService.getTotalRevenue();
        assertEquals(100.50, result);
    }

    @Test
    void getTodayOrdersCount_ShouldReturnCount() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(5);
        Integer result = statsService.getTodayOrdersCount();
        assertEquals(5, result);
    }

    @Test
    void getSalesCsv_ShouldGenerateValidCsv() {
        SalesStatDto stat = new SalesStatDto("Burger", 2L, new BigDecimal("50.00"));

        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of(stat));

        byte[] csvBytes = statsService.getSalesCsv();
        String csvContent = new String(csvBytes);

        assertTrue(csvContent.contains("Nazwa Produktu"));
        assertTrue(csvContent.contains("Burger"));
        assertTrue(csvContent.contains("50.00"));
    }
}