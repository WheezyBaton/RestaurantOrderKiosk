package com.wheezybaton.kiosk_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesStatDto {
    private String productName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}