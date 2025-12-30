package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.OrderRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import com.wheezybaton.kiosk_system.model.OrderItem;
import com.wheezybaton.kiosk_system.model.ModifierAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        CartItemDto itemDto = new CartItemDto(UUID.randomUUID(), 1L, null, BigDecimal.TEN, null, 1, List.of(), List.of(), List.of(), List.of());

        when(cartSession.getItems()).thenReturn(List.of(itemDto));
        when(cartSession.getTotalCartValue()).thenReturn(BigDecimal.TEN);
        when(cartSession.getOrderType()).thenReturn(OrderType.EAT_IN);
        when(productRepo.getReferenceById(1L)).thenReturn(new Product());
        when(orderRepo.save(any(Order.class))).thenAnswer(inv -> {
            ((Order) inv.getArgument(0)).setId(100L);
            return inv.getArgument(0);
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
        Order order = new Order(1L, 0, null, OrderStatus.NEW, null, null, null);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        orderService.promoteOrderStatus(1L);

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        verify(orderRepo).save(order);

        verify(statsService).logStatusChange(1L, OrderStatus.NEW, OrderStatus.IN_PROGRESS);
    }

    @Test
    void promoteOrderStatus_ShouldPromoteInProgressToReady() {
        Order order = new Order(1L, 0, null, OrderStatus.IN_PROGRESS, null, null, null);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        orderService.promoteOrderStatus(1L);

        assertEquals(OrderStatus.READY, order.getStatus());
        verify(orderRepo).save(order);
        verify(statsService).logStatusChange(1L, OrderStatus.IN_PROGRESS, OrderStatus.READY);
    }

    @Test
    void promoteOrderStatus_ShouldPromoteReadyToCompleted() {
        Order order = new Order(1L, 0, null, OrderStatus.READY, null, null, null);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        orderService.promoteOrderStatus(1L);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orderRepo).save(order);
        verify(statsService).logStatusChange(1L, OrderStatus.READY, OrderStatus.COMPLETED);
    }

    @Test
    void getOrdersReady_ShouldReturnOrdersWithStatusReady() {
        Order readyOrder = new Order(100L, 0, null, OrderStatus.READY, null, null, null);

        when(orderRepo.findByStatus(OrderStatus.READY)).thenReturn(List.of(readyOrder));

        List<Order> result = orderService.getOrdersReady();

        assertEquals(1, result.size());
        assertEquals(OrderStatus.READY, result.get(0).getStatus());
        verify(orderRepo).findByStatus(OrderStatus.READY);
    }

    @Test
    void promoteOrderStatus_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepo.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderService.promoteOrderStatus(999L)
        );

        assertEquals("Order not found with ID: 999", ex.getMessage());
    }

    @Test
    void promoteOrderStatus_ShouldSkipLogic_WhenStatusIsTerminal () {
        Order order = new Order(5L, 0, null, OrderStatus.COMPLETED, null, null, null);

        when(orderRepo.findById(5L)).thenReturn(Optional.of(order));

        orderService.promoteOrderStatus(5L);

        verify(orderRepo, never()).save(any());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void placeOrder_ShouldCreateOrder_WithCorrectItemsAndModifiers() {
        Long productId = 1L;
        Long ingredientId = 55L;

        Product productEntity = new Product(productId, null, BigDecimal.TEN, null, null, true, null, null, false);
        when(productRepo.getReferenceById(productId)).thenReturn(productEntity);

        Ingredient ingredientEntity = new Ingredient(ingredientId, null, BigDecimal.ONE);
        when(ingredientRepo.getReferenceById(ingredientId)).thenReturn(ingredientEntity);

        CartItemDto itemDto = new CartItemDto(UUID.randomUUID(), productId, null, null, null, 2, List.of(), List.of(), List.of(ingredientId), List.of());

        when(cartSession.getItems()).thenReturn(List.of(itemDto));
        when(cartSession.getOrderType()).thenReturn(OrderType.EAT_IN);

        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(123L);
            return order;
        });

        orderService.placeOrder();

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepo).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(1, savedOrder.getItems().size());

        OrderItem savedItem = savedOrder.getItems().get(0);
        assertEquals(2, savedItem.getQuantity());
        assertEquals(productEntity, savedItem.getProduct());

        assertEquals(1, savedItem.getModifiers().size());
        assertEquals(ingredientEntity, savedItem.getModifiers().get(0).getIngredient());
        assertEquals(ModifierAction.ADDED, savedItem.getModifiers().get(0).getAction());
    }
}