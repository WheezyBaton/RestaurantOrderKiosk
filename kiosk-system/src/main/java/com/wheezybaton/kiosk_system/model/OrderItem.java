package com.wheezybaton.kiosk_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    private BigDecimal priceAtPurchase;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL)
    private List<OrderItemModifier> modifiers = new ArrayList<>();
}