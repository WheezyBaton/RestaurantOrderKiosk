package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.SalesStatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final JdbcTemplate jdbcTemplate;

    public void createAuditTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS audit_log (id IDENTITY PRIMARY KEY, action VARCHAR(255), timestamp TIMESTAMP)");
    }

    public void logEvent(String action) {
        jdbcTemplate.update("INSERT INTO audit_log (action, timestamp) VALUES (?, ?)",
                action, LocalDateTime.now());
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
                return new SalesStatDto(
                        rs.getString("product_name"),
                        rs.getLong("total_qty"),
                        rs.getBigDecimal("total_rev")
                );
            }
        });
    }

    public byte[] getSalesCsv() {
        try {
            createAuditTable();
            logEvent("EXPORT_CSV_GENERATED");
        } catch (Exception e) {
            System.err.println("Błąd logowania audytu: " + e.getMessage());
        }

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

    public Double getTotalRevenue() {
        String sql = "SELECT SUM(total_amount) FROM orders WHERE status != 'CANCELLED'";
        Double total = jdbcTemplate.queryForObject(sql, Double.class);
        return total != null ? total : 0.0;
    }

    public Integer getTodayOrdersCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE created_at >= CURRENT_DATE";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}