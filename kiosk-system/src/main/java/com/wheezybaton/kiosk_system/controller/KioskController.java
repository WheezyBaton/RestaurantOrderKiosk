package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.model.Order;
import com.wheezybaton.kiosk_system.model.OrderType;
import com.wheezybaton.kiosk_system.model.Product;
import com.wheezybaton.kiosk_system.model.ProductIngredient;
import com.wheezybaton.kiosk_system.service.CartService;
import com.wheezybaton.kiosk_system.service.OrderService;
import com.wheezybaton.kiosk_system.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

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
        cartService.getSession().setOrderType(type);
        return "redirect:/menu";
    }

    @GetMapping("/menu")
    public String showMenu(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("cart", cartService.getSession());
        return "menu";
    }

    @GetMapping("/configure")
    public String showConfiguration(@RequestParam Long productId, Model model) {
        Product product = productService.getProductById(productId);
        model.addAttribute("product", product);
        return "configure";
    }

    @PostMapping("/cart/add-custom")
    public String addCustomProduct(
            @RequestParam Long productId,
            @RequestParam int quantity,
            HttpServletRequest request
    ) {
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
                } catch (NumberFormatException e) {}
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

        cartService.addToCart(productId, addedIds, removedIds, quantity);
        return "redirect:/menu";
    }

    @PostMapping("/cart/clear")
    public String clearCart() {
        cartService.getSession().clear();
        return "redirect:/";
    }

    @GetMapping("/checkout")
    public String showCheckout(Model model) {
        if (cartService.getSession().getItems().isEmpty()) {
            return "redirect:/menu";
        }
        model.addAttribute("cart", cartService.getSession());
        return "checkout";
    }

    @PostMapping("/order/pay")
    public String payAndOrder(RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.placeOrder();
            redirectAttributes.addFlashAttribute("orderNumber", order.getDailyNumber());
            return "redirect:/order-success";
        } catch (Exception e) {
            return "redirect:/menu";
        }
    }

    @GetMapping("/order-success")
    public String showSuccess(Model model) {
        if (!model.containsAttribute("orderNumber")) {
            return "redirect:/";
        }
        return "success";
    }
}