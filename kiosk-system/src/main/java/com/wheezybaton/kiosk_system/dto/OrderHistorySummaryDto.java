package com.wheezybaton.kiosk_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderHistorySummaryDto {
    private Long orderId;
    private List<OrderStatusHistoryDto> historySteps;
}