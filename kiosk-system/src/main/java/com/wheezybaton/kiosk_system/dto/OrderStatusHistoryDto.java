package com.wheezybaton.kiosk_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusHistoryDto {
    private Long orderId;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changedAt;
}