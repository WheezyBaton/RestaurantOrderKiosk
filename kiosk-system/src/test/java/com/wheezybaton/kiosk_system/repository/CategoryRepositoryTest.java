package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;

    @Test
    void shouldSaveCategory() {
        Category cat = new Category(null, "Desery", "img.png", null);
        Category saved = categoryRepo.save(cat);

        assertThat(saved.getId()).isNotNull();
        assertThat(categoryRepo.findAll()).hasSizeGreaterThan(0);
    }
}