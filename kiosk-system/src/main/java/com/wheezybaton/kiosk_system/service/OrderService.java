package com.wheezybaton.kiosk_system.service;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.OrderRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final IngredientRepository ingredientRepo;
    private final CartSession cartSession;
    private final StatsService statsService;
    private final AtomicInteger orderSequence = new AtomicInteger(0);

    public int reserveNextOrderNumber() {
        int nextNumber = orderSequence.updateAndGet(n -> (n >= 999) ? 1 : n + 1);
        log.debug("Generated new daily order number: {}", nextNumber);
        return nextNumber;
    }

    @Transactional
    public Order placeOrder() {
        log.debug("Starting order placement process...");

        List<CartItemDto> sessionItems = cartSession.getItems();

        if (sessionItems.isEmpty()) {
            log.error("Attempted to place an order with an empty cart!");
            throw new RuntimeException("Cart is empty!");
        }

        Order order = new Order();
        order.setTotalAmount(cartSession.getTotalCartValue());
        order.setStatus(OrderStatus.NEW);
        order.setDailyNumber(reserveNextOrderNumber());
        order.setType(cartSession.getOrderType());

        log.info("Initializing new order #{} (Type: {}, Amount: {})",
                order.getDailyNumber(), order.getType(), order.getTotalAmount());

        for (CartItemDto dto : sessionItems) {
            log.debug("Processing item: {} (Quantity: {}, Product ID: {})",
                    dto.getProductName(), dto.getQuantity(), dto.getProductId());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(productRepo.getReferenceById(dto.getProductId()));
            item.setQuantity(dto.getQuantity());
            item.setPriceAtPurchase(dto.getUnitPrice());

            for (Long ingId : dto.getAddedIngredientIds()) {
                log.trace("Adding ingredient (ID: {}) to item {}", ingId, dto.getProductName());

                OrderItemModifier modifier = new OrderItemModifier();
                modifier.setOrderItem(item);
                modifier.setIngredient(ingredientRepo.getReferenceById(ingId));
                modifier.setAction(ModifierAction.ADDED);
                item.getModifiers().add(modifier);
            }

            for (Long ingId : dto.getRemovedIngredientIds()) {
                log.trace("Removing ingredient (ID: {}) from item {}", ingId, dto.getProductName());

                OrderItemModifier modifier = new OrderItemModifier();
                modifier.setOrderItem(item);
                modifier.setIngredient(ingredientRepo.getReferenceById(ingId));
                modifier.setAction(ModifierAction.REMOVED);
                item.getModifiers().add(modifier);
            }
            order.getItems().add(item);
        }

        Order savedOrder = orderRepo.save(order);

        statsService.logStatusChange(savedOrder.getId(), null, OrderStatus.NEW);

        cartSession.clear();

        log.info("Order #{} (ID: {}) successfully saved to database.",
                savedOrder.getDailyNumber(), savedOrder.getId());

        return savedOrder;
    }

    public List<Order> getOrdersInProgress() {
        List<Order> orders = orderRepo.findByStatusInOrderByCreatedAtAsc(
                Arrays.asList(OrderStatus.NEW, OrderStatus.IN_PROGRESS)
        );
        log.debug("Found {} orders in progress.", orders.size());
        return orders;
    }

    public List<Order> getOrdersReady() {
        List<Order> orders = orderRepo.findByStatusInOrderByCreatedAtAsc(List.of(OrderStatus.READY));
        log.debug("Found {} orders ready for pickup.", orders.size());
        return orders;
    }

    @Transactional
    public void promoteOrderStatus(Long orderId) {
        log.debug("Request to promote status for order ID: {}", orderId);

        Order order = orderRepo.findById(orderId).orElseThrow(() -> {
            log.error("Error changing status: Order not found with ID: {}", orderId);
            return new RuntimeException("Order not found with ID: " + orderId);
        });

        OrderStatus oldStatus = order.getStatus();

        switch (order.getStatus()) {
            case NEW:
                order.setStatus(OrderStatus.IN_PROGRESS);
                break;
            case IN_PROGRESS:
                order.setStatus(OrderStatus.READY);
                break;
            case READY:
                order.setStatus(OrderStatus.COMPLETED);
                break;
            default:
                log.warn("Skipped status change for order #{} (ID: {}). Current status: {}",
                        order.getDailyNumber(), order.getId(), order.getStatus());
                break;
        }

        if (oldStatus != order.getStatus()) {
            orderRepo.save(order);

            statsService.logStatusChange(orderId, oldStatus, order.getStatus());

            log.info("Changed status for order #{} (ID: {}): {} -> {}",
                    order.getDailyNumber(), order.getId(), oldStatus, order.getStatus());
        }
    }
}