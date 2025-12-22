package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.OrderRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;
    @Mock
    private ProductRepository productRepo;
    @Mock
    private IngredientRepository ingredientRepo;
    @Mock
    private CartSession cartSession;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_ShouldCreateOrder_WhenCartIsNotEmpty() {
        CartItemDto itemDto = new CartItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        itemDto.setUnitPrice(new BigDecimal("25.00"));

        when(cartSession.getItems()).thenReturn(List.of(itemDto));
        when(cartSession.getTotalCartValue()).thenReturn(new BigDecimal("50.00"));
        when(cartSession.getOrderType()).thenReturn(OrderType.EAT_IN);
        when(orderRepo.countOrdersSince(any(LocalDateTime.class))).thenReturn(10L);
        when(productRepo.getReferenceById(1L)).thenReturn(new Product());
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(123L);
            return o;
        });

        Order result = orderService.placeOrder();

        assertNotNull(result);
        assertEquals(11, result.getDailyNumber());
        assertEquals(OrderStatus.NEW, result.getStatus());
        assertEquals(new BigDecimal("50.00"), result.getTotalAmount());

        verify(cartSession, times(1)).clear();
        verify(orderRepo, times(1)).save(any(Order.class));
    }

    @Test
    void placeOrder_ShouldThrowException_WhenCartIsEmpty() {
        when(cartSession.getItems()).thenReturn(List.of());
        assertThrows(RuntimeException.class, () -> orderService.placeOrder());
        verify(orderRepo, never()).save(any());
    }
}