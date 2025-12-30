package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.dto.CartItemDto;
import com.wheezybaton.kiosk_system.model.Order;
import com.wheezybaton.kiosk_system.model.OrderType;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.service.CartService;
import com.wheezybaton.kiosk_system.service.OrderService;
import com.wheezybaton.kiosk_system.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class KioskController {

    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;

    @GetMapping("/")
    public String showWelcome() {
        return "welcome";
    }

    @PostMapping("/select-type")
    public String selectOrderType(@RequestParam OrderType type) {
        log.info("User selected order type: {}", type);
        cartService.getSession().setOrderType(type);
        return "redirect:/menu";
    }

    @GetMapping("/menu")
    public String showMenu(Model model) {
        log.debug("Loading menu page. Current cart items: {}", cartService.getSession().getItems().size());
        model.addAttribute("products", productService.getAvailableProducts());
        model.addAttribute("cart", cartService.getSession());
        return "menu";
    }

    @GetMapping("/configure")
    public String showConfiguration(@RequestParam Long productId, @RequestParam(required = false) UUID cartItemId, Model model) {
        Product product = productService.getProductById(productId);
        model.addAttribute("product", product);

        Map<Long, Integer> ingredientQuantities = new HashMap<>();
        int mainQuantity = 1;

        if (cartItemId != null) {
            log.debug("Loading existing item configuration from cart: {}", cartItemId);
            CartItemDto item = cartService.getCartItem(cartItemId);
            if (item != null) {
                mainQuantity = item.getQuantity();

                for (ProductIngredient pi : product.getProductIngredients()) {
                    ingredientQuantities.put(pi.getIngredient().getId(), pi.isDefault() ? 1 : 0);
                }

                for (Long removedId : item.getRemovedIngredientIds()) {
                    int current = ingredientQuantities.getOrDefault(removedId, 0);
                    if (current > 0) {
                        ingredientQuantities.put(removedId, current - 1);
                    }
                }

                for (Long addedId : item.getAddedIngredientIds()) {
                    int current = ingredientQuantities.getOrDefault(addedId, 0);
                    ingredientQuantities.put(addedId, current + 1);
                }

                model.addAttribute("cartItemId", cartItemId);
            }
        } else {
            for (ProductIngredient pi : product.getProductIngredients()) {
                ingredientQuantities.put(pi.getIngredient().getId(), pi.isDefault() ? 1 : 0);
            }
        }

        model.addAttribute("ingredientQuantities", ingredientQuantities);
        model.addAttribute("mainQuantity", mainQuantity);

        return "configure";
    }

    @PostMapping("/cart/add-custom")
    public String addCustomProduct(
            @RequestParam Long productId,
            @RequestParam int quantity,
            @RequestParam(required = false) UUID cartItemId, HttpServletRequest request) {
        log.debug("Processing product configuration. ProductID: {}, Quantity: {}", productId, quantity);

        if (cartItemId != null) {
            log.debug("Removing previous version of item {} before update.", cartItemId);
            cartService.removeFromCart(cartItemId);
        }

        Product product = productService.getProductById(productId);

        List<Long> addedIds = new ArrayList<>();
        List<Long> removedIds = new ArrayList<>();

        for (ProductIngredient config : product.getProductIngredients()) {
            Long ingId = config.getIngredient().getId();
            String paramValue = request.getParameter("qty_" + ingId);

            int selectedQty = 0;
            if (paramValue != null) {
                if (paramValue.equals("on")) selectedQty = 1;
                else try {
                    selectedQty = Integer.parseInt(paramValue);
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse quantity for ingredient ID {}: {}", ingId, paramValue);
                }
            }

            int defaultQty = config.isDefault() ? 1 : 0;
            if (selectedQty < defaultQty) {
                removedIds.add(ingId);
            }

            if (selectedQty > defaultQty) {
                int extraCount = selectedQty - defaultQty;
                for (int i = 0; i < extraCount; i++) {
                    addedIds.add(ingId);
                }
            }
        }

        log.info("Adding/Updating cart item. Product: {}, Added Ingredients: {}, Removed Ingredients: {}", product.getName(), addedIds, removedIds);

        cartService.addToCart(productId, addedIds, removedIds, quantity);

        if (cartItemId != null) {
            return "redirect:/checkout";
        }
        return "redirect:/menu";
    }

    @PostMapping("/cart/remove/{itemId}")
    public String removeCartItem(@PathVariable UUID itemId, HttpServletRequest request) {
        log.info("Request to remove item from cart: {}", itemId);
        cartService.removeFromCart(itemId);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/checkout");
    }

    @PostMapping("/cart/clear")
    public String clearCart() {
        log.info("User requested to clear the entire cart.");
        cartService.getSession().clear();
        return "redirect:/";
    }

    @GetMapping("/checkout")
    public String showCheckout(Model model) {
        if (cartService.getSession().getItems().isEmpty()) {
            log.debug("Cart is empty, redirecting from checkout to menu.");
            return "redirect:/menu";
        }
        model.addAttribute("cart", cartService.getSession());
        return "checkout";
    }

    @PostMapping("/order/pay")
    public String payAndOrder(RedirectAttributes redirectAttributes) {
        log.info("Initiating payment and order placement process.");
        try {
            Order order = orderService.placeOrder();
            log.info("Order placed successfully. Daily Number: {}", order.getDailyNumber());
            redirectAttributes.addFlashAttribute("orderNumber", order.getDailyNumber());
            return "redirect:/order-success";
        } catch (Exception e) {
            log.error("Order placement failed: {}", e.getMessage(), e);
            return "redirect:/menu";
        }
    }

    @GetMapping("/order-success")
    public String showSuccess(Model model) {
        if (!model.containsAttribute("orderNumber")) {
            log.warn("Access to success page without order number. Redirecting to welcome.");
            return "redirect:/";
        }
        return "success";
    }
}