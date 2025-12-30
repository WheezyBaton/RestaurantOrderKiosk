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
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StatsService statsService;

    @Test
    void logStatusChange_ShouldExecuteUpdate() {
        statsService.logStatusChange(1L, OrderStatus.NEW, OrderStatus.IN_PROGRESS);
        verify(jdbcTemplate).update(contains("INSERT INTO order_status_history"), eq(1L), eq("NEW"), eq("IN_PROGRESS"), any(LocalDateTime.class));
    }

    @Test
    void logStatusChange_ShouldHandleExceptionGracefully() {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("DB Error"));

        statsService.logStatusChange(1L, OrderStatus.NEW, OrderStatus.IN_PROGRESS);
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
    void getSalesStatsGroupedByMonth_ShouldAggregateData() {
        doAnswer(invocation -> {
            RowCallbackHandler handler = invocation.getArgument(1);

            ResultSet rs = mock(ResultSet.class);

            when(rs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf("2023-10-15 12:00:00"));
            when(rs.getString("product_name")).thenReturn("Burger");
            when(rs.getInt("quantity")).thenReturn(2);
            when(rs.getBigDecimal("price_at_purchase")).thenReturn(BigDecimal.valueOf(20.00));

            handler.processRow(rs);
            return null;
        }).when(jdbcTemplate).query(anyString(), any(RowCallbackHandler.class));

        Map<String, List<SalesStatDto>> result = statsService.getSalesStatsGroupedByMonth();

        assertThat(result).containsKey("2023-10");
        assertThat(result.get("2023-10")).hasSize(1);

        SalesStatDto stat = result.get("2023-10").get(0);
        assertThat(stat.getProductName()).isEqualTo("Burger");
        assertThat(stat.getTotalQuantity()).isEqualTo(2);
        assertThat(stat.getTotalRevenue()).isEqualTo(BigDecimal.valueOf(40.0));
    }

    @Test
    void getSalesCsv_ShouldGenerateCsvContent() {
        SalesStatDto stat = new SalesStatDto("Fries", 10L, BigDecimal.valueOf(50));
        when(jdbcTemplate.query(contains("SELECT"), any(RowMapper.class)))
                .thenReturn(List.of(stat));

        byte[] csv = statsService.getSalesCsv();
        String csvString = new String(csv);

        assertThat(csvString).contains("Product Name,Quantity Sold,Revenue");
        assertThat(csvString).contains("Fries,10,50");
    }

    @Test
    void getTotalRevenue_ShouldReturnAmount() {
        when(jdbcTemplate.queryForObject(contains("SELECT SUM(total_amount)"), eq(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(500));

        assertThat(statsService.getTotalRevenue()).isEqualTo(BigDecimal.valueOf(500));
    }

    @Test
    void getTotalRevenue_ShouldReturnZeroOnException() {
        when(jdbcTemplate.queryForObject(contains("SELECT SUM(total_amount)"), eq(BigDecimal.class)))
                .thenThrow(new RuntimeException("DB Connection Error"));

        assertThat(statsService.getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getMonthlyOrdersCount_ShouldReturnCount() {
        when(jdbcTemplate.queryForObject(contains("SELECT COUNT(*)"), eq(Long.class)))
                .thenReturn(10L);

        assertThat(statsService.getMonthlyOrdersCount()).isEqualTo(10L);
    }

    @Test
    void getMonthlyOrdersCount_ShouldReturnZeroOnException() {
        when(jdbcTemplate.queryForObject(contains("SELECT COUNT(*)"), eq(Long.class)))
                .thenThrow(new RuntimeException("Error"));

        assertThat(statsService.getMonthlyOrdersCount()).isEqualTo(0L);
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

    @Test
    void getSalesStatsGroupedByMonth_ShouldAggregateDuplicateProductsInSameMonth() {
        doAnswer(invocation -> {
            RowCallbackHandler rch = invocation.getArgument(1);

            ResultSet rs1 = mockResultRow("2023-11-01 10:00:00", "Burger", 1, new BigDecimal("10.00"));
            rch.processRow(rs1);

            ResultSet rs2 = mockResultRow("2023-11-02 12:00:00", "Burger", 2, new BigDecimal("10.00"));
            rch.processRow(rs2);

            return null;
        }).when(jdbcTemplate).query(anyString(), any(RowCallbackHandler.class));

        Map<String, List<SalesStatDto>> result = statsService.getSalesStatsGroupedByMonth();

        assertNotNull(result);
        assertTrue(result.containsKey("2023-11"));

        List<SalesStatDto> stats = result.get("2023-11");
        assertEquals(1, stats.size(), "Powinien być tylko 1 wpis dla Burgera (zaggregoowany)");

        SalesStatDto burgerStat = stats.get(0);
        assertEquals("Burger", burgerStat.getProductName());
        assertEquals(3L, burgerStat.getTotalQuantity(), "Ilość powinna być zsumowana (1 + 2)");
        assertEquals(new BigDecimal("30.00"), burgerStat.getTotalRevenue(), "Przychód powinien być zsumowany (10 + 20)");
    }

    private ResultSet mockResultRow(String dateStr, String productName, int qty, BigDecimal price) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(dateStr));
        when(rs.getString("product_name")).thenReturn(productName);
        when(rs.getInt("quantity")).thenReturn(qty);
        when(rs.getBigDecimal("price_at_purchase")).thenReturn(price);
        return rs;
    }

    @Test
    void getMonthlyRevenue_ShouldReturnZero_OnDbException() {
        when(jdbcTemplate.queryForObject(contains("EXTRACT(MONTH FROM created_at)"), eq(BigDecimal.class)))
                .thenThrow(new RuntimeException("Database error"));

        BigDecimal result = statsService.getMonthlyRevenue();

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void getTodayOrdersCount_ShouldReturnZero_OnDbException() {
        when(jdbcTemplate.queryForObject(contains("COUNT(*) FROM orders"), eq(Long.class)))
                .thenThrow(new RuntimeException("Database down"));

        Long result = statsService.getTodayOrdersCount();

        assertEquals(0L, result);
    }

    @Test
    void getTodayRevenue_ShouldReturnZero_OnDbException() {
        when(jdbcTemplate.queryForObject(contains("SUM(total_amount)"), eq(BigDecimal.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        BigDecimal result = statsService.getTodayRevenue();

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void getMonthlyRevenue_ShouldReturnAmount() {
        when(jdbcTemplate.queryForObject(contains("EXTRACT(MONTH FROM created_at)"), eq(BigDecimal.class)))
                .thenReturn(new BigDecimal("1500.50"));

        BigDecimal result = statsService.getMonthlyRevenue();
        assertEquals(new BigDecimal("1500.50"), result);
    }

    @Test
    void getTodayOrdersCount_ShouldReturnCount() {
        when(jdbcTemplate.queryForObject(contains("COUNT(*)"), eq(Long.class)))
                .thenReturn(123L);

        assertEquals(123L, statsService.getTodayOrdersCount());
    }

    @Test
    void getTodayRevenue_ShouldReturnAmount() {
        when(jdbcTemplate.queryForObject(contains("SUM(total_amount)"), eq(BigDecimal.class)))
                .thenReturn(new BigDecimal("500.00"));

        assertEquals(new BigDecimal("500.00"), statsService.getTodayRevenue());
    }

    @Test
    void getGroupedStatusHistory_ShouldReturnSortedList() {
        OrderStatusHistoryDto dto1 = new OrderStatusHistoryDto(1L, "NEW", "PREPARING", LocalDateTime.now());
        when(jdbcTemplate.query(contains("SELECT order_id"), any(RowMapper.class)))
                .thenReturn(List.of(dto1));

        List<OrderHistorySummaryDto> result = statsService.getGroupedStatusHistory();

        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0).getOrderId());
    }
}