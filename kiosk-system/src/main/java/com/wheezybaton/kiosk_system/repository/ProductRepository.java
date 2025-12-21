package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByDeletedFalse();
}