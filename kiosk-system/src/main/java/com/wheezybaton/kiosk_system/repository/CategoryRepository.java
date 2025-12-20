package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}