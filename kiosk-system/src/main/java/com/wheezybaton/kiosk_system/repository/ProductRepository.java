package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}