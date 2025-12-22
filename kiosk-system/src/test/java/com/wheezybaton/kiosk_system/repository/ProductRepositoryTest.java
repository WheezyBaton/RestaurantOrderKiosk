package com.wheezybaton.kiosk_system.repository;

import com.wheezybaton.kiosk_system.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepo;

    @Test
    void shouldSaveAndFindProduct() {
        Product product = new Product(null, "Test Burger", new BigDecimal("10.00"), "Desc", "img.jpg", null, null, false);
        Product saved = productRepo.save(product);

        Optional<Product> found = productRepo.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Burger");
    }

    @Test
    void findByDeletedFalse_ShouldReturnOnlyActiveProducts() {
        Product active = new Product(null, "Active", new BigDecimal("10"), "", "", null, null, false);
        Product deleted = new Product(null, "Deleted", new BigDecimal("10"), "", "", null, null, true); // deleted=true
        productRepo.saveAll(List.of(active, deleted));

        List<Product> result = productRepo.findByDeletedFalse();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Active");
    }

    @Test
    void findByDeletedFalse_Pagination_ShouldReturnPage() {
        for (int i = 0; i < 5; i++) {
            productRepo.save(new Product(null, "Burger " + i, new BigDecimal("10"), "", "", null, null, false));
        }

        Page<Product> page = productRepo.findByDeletedFalse(PageRequest.of(0, 2));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }
}