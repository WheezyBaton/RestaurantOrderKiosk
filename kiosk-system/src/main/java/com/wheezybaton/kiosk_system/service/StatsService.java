package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.OrderHistorySummaryDto;
import com.wheezybaton.kiosk_system.dto.OrderStatusHistoryDto;
import com.wheezybaton.kiosk_system.dto.SalesStatDto;
import com.wheezybaton.kiosk_system.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void logStatusChange(Long orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        log.debug("Attempting to log status change for order #{}: {} -> {}", orderId, oldStatus, newStatus);
        try {
            jdbcTemplate.update("INSERT INTO order_status_history (order_id, old_status, new_status, changed_at) VALUES (?, ?, ?, ?)",
                    orderId,
                    oldStatus != null ? oldStatus.name() : null,
                    newStatus.name(),
                    LocalDateTime.now());
            log.trace("Status change logged successfully.");
        } catch (Exception e) {
            log.error("Failed to write status history for order #{}. Error: {}", orderId, e.getMessage(), e);
        }
    }

    public List<SalesStatDto> getSalesStats() {
        log.debug("Fetching detailed sales statistics by product...");

        String sql = """
            SELECT 
                p.name AS product_name, 
                SUM(oi.quantity) AS total_qty, 
                SUM(oi.price_at_purchase * oi.quantity) AS total_rev 
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN orders o ON oi.order_id = o.id
            WHERE o.status != 'CANCELLED' 
            GROUP BY p.name
            ORDER BY total_rev DESC
        """;

        List<SalesStatDto> stats = jdbcTemplate.query(sql, new RowMapper<SalesStatDto>() {
            @Override
            public SalesStatDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                BigDecimal revenue = rs.getBigDecimal("total_rev");
                return new SalesStatDto(
                        rs.getString("product_name"),
                        rs.getLong("total_qty"),
                        revenue != null ? revenue : BigDecimal.ZERO
                );
            }
        });

        log.debug("Retrieved sales statistics for {} distinct products.", stats.size());
        return stats;
    }

    public Map<String, List<SalesStatDto>> getSalesStatsGroupedByMonth() {
        log.debug("Fetching sales statistics grouped by month...");

        String sql = """
            SELECT 
                o.created_at, 
                p.name AS product_name, 
                oi.quantity, 
                oi.price_at_purchase 
            FROM order_item oi
            JOIN product p ON oi.product_id = p.id
            JOIN orders o ON oi.order_id = o.id
            WHERE o.status != 'CANCELLED'
        """;

        Map<String, Map<String, SalesStatDto>> rawMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        jdbcTemplate.query(sql, (rs) -> {
            LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
            String month = createdAt.format(formatter);
            String productName = rs.getString("product_name");
            int quantity = rs.getInt("quantity");
            BigDecimal price = rs.getBigDecimal("price_at_purchase");
            BigDecimal revenue = price.multiply(BigDecimal.valueOf(quantity));

            rawMap.computeIfAbsent(month, k -> new HashMap<>())
                    .compute(productName, (k, v) -> {
                        if (v == null) {
                            return new SalesStatDto(productName, (long) quantity, revenue);
                        } else {
                            v.setTotalQuantity(v.getTotalQuantity() + quantity);
                            v.setTotalRevenue(v.getTotalRevenue().add(revenue));
                            return v;
                        }
                    });
        });

        Map<String, List<SalesStatDto>> result = new LinkedHashMap<>();

        rawMap.entrySet().stream()
                .sorted(Map.Entry.<String, Map<String, SalesStatDto>>comparingByKey().reversed())
                .forEach(entry -> {
                    List<SalesStatDto> stats = new ArrayList<>(entry.getValue().values());
                    stats.sort(Comparator.comparing(SalesStatDto::getTotalRevenue).reversed());
                    result.put(entry.getKey(), stats);
                });

        log.debug("Aggregated sales stats for {} months.", result.size());
        return result;
    }

    @Transactional
    public byte[] getSalesCsv() {
        log.info("Starting CSV export generation...");
        log.debug("CSV Export triggered.");

        List<SalesStatDto> stats = getSalesStats();
        StringBuilder csv = new StringBuilder();

        csv.append("Product Name,Quantity Sold,Revenue (PLN)\n");

        for (SalesStatDto stat : stats) {
            csv.append(stat.getProductName()).append(",")
                    .append(stat.getTotalQuantity()).append(",")
                    .append(stat.getTotalRevenue()).append("\n");
        }

        byte[] result = csv.toString().getBytes();
        log.info("CSV generation completed. Size: {} bytes.", result.length);
        return result;
    }

    public BigDecimal getTotalRevenue() {
        log.debug("Calculating total all-time revenue...");
        String sql = "SELECT SUM(total_amount) FROM orders WHERE status != 'CANCELLED'";
        try {
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating total revenue: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getMonthlyRevenue() {
        log.debug("Calculating revenue for the current month...");
        String sql = "SELECT SUM(total_amount) FROM orders WHERE status != 'CANCELLED' AND EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM CURRENT_DATE)";
        try {
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating monthly revenue: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    public Long getMonthlyOrdersCount() {
        log.debug("Counting orders for the current month...");
        String sql = "SELECT COUNT(*) FROM orders WHERE status != 'CANCELLED' AND EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM CURRENT_DATE)";
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Error counting monthly orders: {}", e.getMessage(), e);
            return 0L;
        }
    }

    public Long getTodayOrdersCount() {
        log.debug("Counting orders for today...");
        String sql = "SELECT COUNT(*) FROM orders WHERE created_at >= CURRENT_DATE";
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Error counting today's orders: {}", e.getMessage(), e);
            return 0L;
        }
    }

    public BigDecimal getTodayRevenue() {
        log.debug("Calculating revenue for today...");
        String sql = "SELECT SUM(total_amount) FROM orders WHERE created_at >= CURRENT_DATE AND status != 'CANCELLED'";
        try {
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error calculating today's revenue: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    public List<OrderHistorySummaryDto> getGroupedStatusHistory() {
        String sql = "SELECT order_id, old_status, new_status, changed_at FROM order_status_history ORDER BY changed_at DESC LIMIT 100";

        List<OrderStatusHistoryDto> rawLogs = jdbcTemplate.query(sql, (rs, rowNum) -> new OrderStatusHistoryDto(
                rs.getLong("order_id"),
                rs.getString("old_status"),
                rs.getString("new_status"),
                rs.getTimestamp("changed_at").toLocalDateTime()
        ));

        return rawLogs.stream()
                .collect(Collectors.groupingBy(OrderStatusHistoryDto::getOrderId))
                .entrySet().stream()
                .map(entry -> {
                    Long orderId = entry.getKey();
                    List<OrderStatusHistoryDto> steps = entry.getValue();
                    steps.sort(Comparator.comparing(OrderStatusHistoryDto::getChangedAt));
                    return new OrderHistorySummaryDto(orderId, steps);
                })
                .sorted((o1, o2) -> {
                    if (o1.getHistorySteps().isEmpty() || o2.getHistorySteps().isEmpty()) return 0;
                    LocalDateTime lastUpdate1 = o1.getHistorySteps().get(o1.getHistorySteps().size() - 1).getChangedAt();
                    LocalDateTime lastUpdate2 = o2.getHistorySteps().get(o2.getHistorySteps().size() - 1).getChangedAt();
                    return lastUpdate2.compareTo(lastUpdate1);
                })
                .collect(Collectors.toList());
    }
}