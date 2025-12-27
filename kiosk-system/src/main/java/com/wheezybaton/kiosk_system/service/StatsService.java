package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.SalesStatDto;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void createAuditTable() {
        log.debug("Checking/Creating audit_log table schema...");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS audit_log (id SERIAL PRIMARY KEY, action VARCHAR(255), timestamp TIMESTAMP)");
    }

    @Transactional
    public void logEvent(String action) {
        log.debug("Attempting to log audit event: {}", action);
        try {
            createAuditTable();
            jdbcTemplate.update("INSERT INTO audit_log (action, timestamp) VALUES (?, ?)",
                    action, LocalDateTime.now());
            log.trace("Audit event logged successfully.");
        } catch (Exception e) {
            log.error("Failed to write audit log entry for action: {}. Error: {}", action, e.getMessage(), e);
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

    @Transactional
    public byte[] getSalesCsv() {
        log.info("Starting CSV export generation...");
        logEvent("EXPORT_CSV_GENERATED");

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
}