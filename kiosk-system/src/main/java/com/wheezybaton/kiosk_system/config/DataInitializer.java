package com.wheezybaton.kiosk_system.config;

import com.wheezybaton.kiosk_system.model.*;
import com.wheezybaton.kiosk_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepo;
    private final IngredientRepository ingredientRepo;
    private final OrderRepository orderRepo;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (orderRepo.count() == 0) {
            System.out.println("Brak zamówień w bazie. Generowanie przykładowych zamówień...");

            List<Product> products = productRepo.findAll();
            List<Ingredient> ingredients = ingredientRepo.findAll();

            if (products.isEmpty() || ingredients.isEmpty()) {
                System.out.println("Brak produktów lub składników w bazie! Upewnij się, że data.sql został załadowany.");
                return;
            }

            generateFakeOrders(products, ingredients);
        } else {
            System.out.println("Zamówienia już istnieją. Pomijam generowanie.");
        }
    }

    private void generateFakeOrders(List<Product> products, List<Ingredient> extraIngredients) {
        Random rand = new Random();
        int ordersCount = 25;

        for (int i = 1; i <= ordersCount; i++) {
            Order order = new Order();
            order.setDailyNumber(i);

            int statusRoll = rand.nextInt(10);
            if (statusRoll < 6) order.setStatus(OrderStatus.COMPLETED);
            else if (statusRoll < 8) order.setStatus(OrderStatus.READY);
            else order.setStatus(OrderStatus.NEW);

            order.setType(rand.nextBoolean() ? OrderType.EAT_IN : OrderType.TAKE_AWAY);
            order.setCreatedAt(LocalDateTime.now().minusMinutes(rand.nextInt(480)));

            List<OrderItem> items = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            int itemsInOrder = rand.nextInt(4) + 1;

            for (int j = 0; j < itemsInOrder; j++) {
                Product randomProduct = products.get(rand.nextInt(products.size()));

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProduct(randomProduct);
                item.setQuantity(rand.nextInt(2) + 1);

                BigDecimal itemPrice = randomProduct.getBasePrice();

                if (rand.nextBoolean()) {
                    Ingredient extra = extraIngredients.get(rand.nextInt(extraIngredients.size()));
                    OrderItemModifier mod = new OrderItemModifier();
                    mod.setOrderItem(item);
                    mod.setIngredient(extra);
                    mod.setAction(ModifierAction.ADDED);

                    if (item.getModifiers() == null) {
                        item.setModifiers(new ArrayList<>());
                    }
                    item.getModifiers().add(mod);

                    itemPrice = itemPrice.add(extra.getPrice());
                }
                item.setPriceAtPurchase(itemPrice);
                items.add(item);
                totalAmount = totalAmount.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            order.setItems(items);
            order.setTotalAmount(totalAmount);
            orderRepo.save(order);
        }
        System.out.println("Wygenerowano " + ordersCount + " przykładowych zamówień.");
    }
}