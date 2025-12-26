package com.wheezybaton.kiosk_system.config;

import com.wheezybaton.kiosk_system.model.Ingredient;
import com.wheezybaton.kiosk_system.model.Order;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.repository.IngredientRepository;
import com.wheezybaton.kiosk_system.repository.OrderRepository;
import com.wheezybaton.kiosk_system.repository.ProductRepository;
import com.wheezybaton.kiosk_system.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private ProductRepository productRepo;

    @Mock
    private IngredientRepository ingredientRepo;

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void run_ShouldGenerateOrders_WhenNoOrdersAndDataExists() throws Exception {
        when(orderRepo.count()).thenReturn(0L);

        Product mockProduct = new Product();
        mockProduct.setBasePrice(BigDecimal.TEN);
        when(productRepo.findAll()).thenReturn(List.of(mockProduct));

        Ingredient mockIngredient = new Ingredient();
        mockIngredient.setPrice(BigDecimal.ONE);
        when(ingredientRepo.findAll()).thenReturn(List.of(mockIngredient));

        when(orderService.reserveNextOrderNumber()).thenReturn(1);

        dataInitializer.run();

        verify(orderRepo, atLeastOnce()).save(any(Order.class));
        verify(orderService, atLeastOnce()).reserveNextOrderNumber();
    }

    @Test
    void run_ShouldDoNothing_WhenOrdersAlreadyExist() throws Exception {
        when(orderRepo.count()).thenReturn(5L);

        dataInitializer.run();

        verify(productRepo, never()).findAll();
        verify(orderRepo, never()).save(any(Order.class));
    }

    @Test
    void run_ShouldDoNothing_WhenNoProductsOrIngredients() throws Exception {
        when(orderRepo.count()).thenReturn(0L);
        when(productRepo.findAll()).thenReturn(Collections.emptyList());
        when(ingredientRepo.findAll()).thenReturn(Collections.emptyList());

        dataInitializer.run();

        verify(orderRepo, never()).save(any(Order.class));
    }
}