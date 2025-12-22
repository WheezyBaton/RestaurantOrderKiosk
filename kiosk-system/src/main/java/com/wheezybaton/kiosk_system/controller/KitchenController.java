package com.wheezybaton.kiosk_system.controller;

import com.wheezybaton.kiosk_system.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class KitchenController {

    private final OrderService orderService;

    @GetMapping("/board")
    public String showOrderBoard(Model model) {
        model.addAttribute("inProgress", orderService.getOrdersInProgress());
        model.addAttribute("ready", orderService.getOrdersReady());
        return "board";
    }

    @GetMapping("/kitchen")
    public String showKitchenPanel(Model model) {
        model.addAttribute("inProgress", orderService.getOrdersInProgress());
        model.addAttribute("ready", orderService.getOrdersReady());
        return "kitchen";
    }

    @PostMapping("/kitchen/promote/{id}")
    public String promoteOrder(@PathVariable Long id) {
        orderService.promoteOrderStatus(id);
        return "redirect:/kitchen";
    }
}