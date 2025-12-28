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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepo;
    @Mock private ProductRepository productRepo;
    @Mock private IngredientRepository ingredientRepo;
    @Mock private CartSession cartSession;
    @Mock private StatsService statsService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_ShouldCreateOrder_WhenCartIsNotEmpty() {
        CartItemDto itemDto = new CartItemDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(1);
        itemDto.setUnitPrice(BigDecimal.TEN);
        itemDto.setAddedIngredientIds(Collections.emptyList());
        itemDto.setRemovedIngredientIds(Collections.emptyList());

        when(cartSession.getItems()).thenReturn(List.of(itemDto));
        when(cartSession.getTotalCartValue()).thenReturn(BigDecimal.TEN);
        when(cartSession.getOrderType()).thenReturn(OrderType.EAT_IN);
        when(productRepo.getReferenceById(1L)).thenReturn(new Product());
        when(orderRepo.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });

        Order result = orderService.placeOrder();

        assertNotNull(result);
        assertEquals(1, result.getDailyNumber());
        assertEquals(OrderStatus.NEW, result.getStatus());
        verify(cartSession).clear();
        verify(statsService).logStatusChange(anyLong(), isNull(), eq(OrderStatus.NEW));
    }

    @Test
    void placeOrder_ShouldThrow_WhenCartIsEmpty() {
        when(cartSession.getItems()).thenReturn(Collections.emptyList());
        assertThrows(RuntimeException.class, () -> orderService.placeOrder());
    }

    @Test
    void getOrdersInProgress_ShouldCallRepository() {
        when(orderRepo.findByStatusInOrderByCreatedAtAsc(anyList()))
                .thenReturn(List.of(new Order()));

        List<Order> result = orderService.getOrdersInProgress();

        assertFalse(result.isEmpty());
        verify(orderRepo).findByStatusInOrderByCreatedAtAsc(anyList());
    }

    @Test
    void promoteOrderStatus_ShouldPromoteNewToInProgress() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.NEW);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        orderService.promoteOrderStatus(1L);

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        verify(orderRepo).save(order);

        verify(statsService).logStatusChange(1L, OrderStatus.NEW, OrderStatus.IN_PROGRESS);
    }

    @Test
    void promoteOrderStatus_ShouldPromoteInProgressToReady() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.IN_PROGRESS);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        orderService.promoteOrderStatus(1L);

        assertEquals(OrderStatus.READY, order.getStatus());
        verify(orderRepo).save(order);
        verify(statsService).logStatusChange(1L, OrderStatus.IN_PROGRESS, OrderStatus.READY);
    }

    @Test
    void promoteOrderStatus_ShouldPromoteReadyToCompleted() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.READY);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        orderService.promoteOrderStatus(1L);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orderRepo).save(order);
        verify(statsService).logStatusChange(1L, OrderStatus.READY, OrderStatus.COMPLETED);
    }
}