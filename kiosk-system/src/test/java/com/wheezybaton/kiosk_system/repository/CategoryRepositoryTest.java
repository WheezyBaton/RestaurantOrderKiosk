package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepo;

    @Test
    void shouldSaveAndFindCategory() {
        Category cat = new Category(null, "Unikalne Desery", "img.png", null);

        Category saved = categoryRepo.save(cat);

        assertThat(saved.getId()).isNotNull();

        Optional<Category> found = categoryRepo.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Unikalne Desery");
    }

    @Test
    void shouldThrowException_WhenNameIsNull() {
        Category invalidCat = new Category(null, null, "img.png", null);

        assertThatThrownBy(() -> categoryRepo.saveAndFlush(invalidCat))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}