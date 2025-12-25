package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Category;
import com.wheezybaton.kiosk_system.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByDeletedFalseOrderByIdAsc();
    List<Product> findByDeletedFalse();
    List<Product> findByDeletedFalseAndAvailableTrue();
    Page<Product> findByDeletedFalse(Pageable pageable);
    List<Product> findByCategoryAndDeletedFalseAndAvailableTrue(Category category);
    List<Product> findByCategoryAndDeletedFalse(Category category);
}