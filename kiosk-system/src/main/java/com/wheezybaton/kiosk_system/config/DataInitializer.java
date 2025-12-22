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

    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final IngredientRepository ingredientRepo;
    private final ProductIngredientRepository productIngredientRepo;
    private final OrderRepository orderRepo;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        orderRepo.deleteAll();
        productIngredientRepo.deleteAll();
        productRepo.deleteAll();
        ingredientRepo.deleteAll();
        categoryRepo.deleteAll();

        System.out.println("Wyczyszczono bazę danych.");

        Category catBurgers = new Category(null, "Burgery", "burger.png", null);
        Category catSides = new Category(null, "Dodatki", "fries.png", null);
        Category catDrinks = new Category(null, "Napoje", "drink.png", null);
        categoryRepo.saveAll(List.of(catBurgers, catSides, catDrinks));

        Ingredient bun = saveIng("Bułka Brioche", "0.00");
        Ingredient beef = saveIng("Wołowina 100%", "5.00");
        Ingredient chicken = saveIng("Kurczak w panierce", "4.00");
        Ingredient vegePatty = saveIng("Kotlet Roślinny", "6.00");
        Ingredient cheese = saveIng("Ser Cheddar", "2.00");
        Ingredient bacon = saveIng("Bekon", "3.00");
        Ingredient onion = saveIng("Cebula", "0.00");
        Ingredient lettuce = saveIng("Sałata", "0.00");
        Ingredient tomato = saveIng("Pomidor", "0.00");
        Ingredient pickle = saveIng("Ogórek kiszony", "0.00");
        Ingredient spicySauce = saveIng("Sos Ostry", "1.00");
        Ingredient mayo = saveIng("Majonez", "0.00");
        Ingredient ketchup = saveIng("Ketchup", "0.00");
        Ingredient friesIng = saveIng("Ziemniaki", "0.00");
        Ingredient colaIng = saveIng("Syrop Cola", "0.00");

        Product p1 = saveProduct("Classic Burger", "25.00", "Klasyczna wołowina z warzywami", "classic.jpg", catBurgers);
        saveConfig(p1, bun, true, 1);
        saveConfig(p1, beef, true, 2);
        saveConfig(p1, cheese, true, 2);
        saveConfig(p1, onion, true, 1);
        saveConfig(p1, lettuce, true, 1);
        saveConfig(p1, tomato, true, 1);
        saveConfig(p1, bacon, false, 3);

        Product p2 = saveProduct("Bacon BBQ Master", "32.00", "Podwójny bekon i ostry sos", "bacon.jpg", catBurgers);
        saveConfig(p2, bun, true, 1);
        saveConfig(p2, beef, true, 3);
        saveConfig(p2, cheese, true, 2);
        saveConfig(p2, bacon, true, 5);
        saveConfig(p2, spicySauce, true, 2);
        saveConfig(p2, onion, true, 1);

        Product p3 = saveProduct("Chicken Crunch", "22.00", "Chrupiący kurczak z majonezem", "chicken.jpg", catBurgers);
        saveConfig(p3, bun, true, 1);
        saveConfig(p3, chicken, true, 2);
        saveConfig(p3, lettuce, true, 1);
        saveConfig(p3, mayo, true, 1);
        saveConfig(p3, tomato, false, 1);

        Product p4 = saveProduct("Vege Delight", "28.00", "100% roślinny, 100% smaku", "vege.jpg", catBurgers);
        saveConfig(p4, bun, true, 1);
        saveConfig(p4, vegePatty, true, 1);
        saveConfig(p4, lettuce, true, 2);
        saveConfig(p4, tomato, true, 2);
        saveConfig(p4, cheese, false, 1);

        Product p5 = saveProduct("Frytki Belgijskie", "12.00", "Grubo krojone, chrupiące", "fries.jpg", catSides);
        saveConfig(p5, friesIng, true, 1);
        saveConfig(p5, spicySauce, false, 2);
        saveConfig(p5, mayo, false, 2);

        Product p6 = saveProduct("Coca-Cola 0.5L", "8.00", "Zimna i orzeźwiająca", "cola.jpg", catDrinks);
        saveConfig(p6, colaIng, true, 1);

        System.out.println("Produkty utworzone.");

        generateFakeOrders(List.of(p1, p2, p3, p4, p5, p6), List.of(bacon, cheese, onion));
    }

    private Ingredient saveIng(String name, String price) {
        return ingredientRepo.save(new Ingredient(null, name, new BigDecimal(price)));
    }

    private Product saveProduct(String name, String price, String desc, String img, Category cat) {
        Product p = new Product(null, name, new BigDecimal(price), desc, img, cat, null, false);
        return productRepo.save(p);
    }

    private void saveConfig(Product p, Ingredient i, boolean isDefault, int maxQty) {
        ProductIngredient pi = new ProductIngredient(null, p, i, isDefault, 1, null, maxQty);
        productIngredientRepo.save(pi);
    }

    private void generateFakeOrders(List<Product> products, List<Ingredient> extraIngredients) {
        Random rand = new Random();
        int ordersCount = 25;

        for (int i = 1; i <= ordersCount; i++) {
            Order order = new Order();
            order.setDailyNumber(i);
            order.setStatus(rand.nextInt(10) > 1 ? OrderStatus.COMPLETED : OrderStatus.CANCELLED);
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