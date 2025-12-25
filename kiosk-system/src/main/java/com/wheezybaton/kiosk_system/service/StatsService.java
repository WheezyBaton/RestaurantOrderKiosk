package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.SalesStatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final JdbcTemplate jdbcTemplate;

    public void createAuditTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS audit_log (id SERIAL PRIMARY KEY, action VARCHAR(255), timestamp TIMESTAMP)");
    }

    public void logEvent(String action) {
        try {
            createAuditTable();
            jdbcTemplate.update("INSERT INTO audit_log (action, timestamp) VALUES (?, ?)",
                    action, LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Nie udało się zapisać logu audytowego: " + e.getMessage());
        }
    }

    public List<SalesStatDto> getSalesStats() {
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

        return jdbcTemplate.query(sql, new RowMapper<SalesStatDto>() {
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
    }

    public byte[] getSalesCsv() {
        logEvent("EXPORT_CSV_GENERATED");

        List<SalesStatDto> stats = getSalesStats();
        StringBuilder csv = new StringBuilder();

        csv.append("Nazwa Produktu,Sprzedana Ilosc,Przychod (PLN)\n");

        for (SalesStatDto stat : stats) {
            csv.append(stat.getProductName()).append(",")
                    .append(stat.getTotalQuantity()).append(",")
                    .append(stat.getTotalRevenue()).append("\n");
        }

        return csv.toString().getBytes();
    }

    public BigDecimal getTotalRevenue() {
        String sql = "SELECT SUM(total_amount) FROM orders WHERE status != 'CANCELLED'";
        try {
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getMonthlyRevenue() {
        String sql = "SELECT SUM(total_amount) FROM orders WHERE status != 'CANCELLED' AND EXTRACT(MONTH FROM created_at) = EXTRACT(MONTH FROM CURRENT_DATE)";
        try {
            BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public Long getTodayOrdersCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE created_at >= CURRENT_DATE";
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}