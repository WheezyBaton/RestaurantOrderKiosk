package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.OrderRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final IngredientRepository ingredientRepo;
    private final CartSession cartSession;
    private final AtomicInteger orderSequence = new AtomicInteger(0);

    public int reserveNextOrderNumber() {
        return orderSequence.updateAndGet(n -> (n >= 999) ? 1 : n + 1);
    }

    @Transactional
    public Order placeOrder() {
        List<CartItemDto> sessionItems = cartSession.getItems();

        if (sessionItems.isEmpty()) {
            throw new RuntimeException("Koszyk jest pusty!");
        }

        Order order = new Order();
        order.setTotalAmount(cartSession.getTotalCartValue());
        order.setStatus(OrderStatus.NEW);

        order.setDailyNumber(reserveNextOrderNumber());

        order.setType(cartSession.getOrderType());

        for (CartItemDto dto : sessionItems) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(productRepo.getReferenceById(dto.getProductId()));
            item.setQuantity(dto.getQuantity());
            item.setPriceAtPurchase(dto.getUnitPrice());

            for (Long ingId : dto.getAddedIngredientIds()) {
                OrderItemModifier modifier = new OrderItemModifier();
                modifier.setOrderItem(item);
                modifier.setIngredient(ingredientRepo.getReferenceById(ingId));
                modifier.setAction(ModifierAction.ADDED);
                item.getModifiers().add(modifier);
            }

            for (Long ingId : dto.getRemovedIngredientIds()) {
                OrderItemModifier modifier = new OrderItemModifier();
                modifier.setOrderItem(item);
                modifier.setIngredient(ingredientRepo.getReferenceById(ingId));
                modifier.setAction(ModifierAction.REMOVED);
                item.getModifiers().add(modifier);
            }
            order.getItems().add(item);
        }

        Order savedOrder = orderRepo.save(order);
        cartSession.clear();
        return savedOrder;
    }

    public List<Order> getOrdersInProgress() {
        return orderRepo.findByStatusInOrderByCreatedAtAsc(
                Arrays.asList(OrderStatus.NEW, OrderStatus.IN_PROGRESS)
        );
    }

    public List<Order> getOrdersReady() {
        return orderRepo.findByStatus(OrderStatus.READY);
    }

    @Transactional
    public void promoteOrderStatus(Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow();

        switch (order.getStatus()) {
            case NEW:
            case IN_PROGRESS:
                order.setStatus(OrderStatus.READY);
                break;
            case READY:
                order.setStatus(OrderStatus.COMPLETED);
                break;
            default:
                break;
        }
        orderRepo.save(order);
    }
}