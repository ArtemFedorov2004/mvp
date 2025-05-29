package io.github.artemfedorov2004.onlinestoreservice.repository;

import io.github.artemfedorov2004.onlinestoreservice.entity.Customer;
import io.github.artemfedorov2004.onlinestoreservice.entity.Product;
import io.github.artemfedorov2004.onlinestoreservice.entity.Review;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Sql("/sql/reviews.sql")
@Transactional
class ReviewRepositoryIT {

    @Autowired
    ReviewRepository reviewRepository;

    @Test
    void findAllByProductId_ReturnsReviews() {
        // given
        Product product = new Product(1L, "Ананас", BigDecimal.valueOf(100));
        Customer customer = new Customer(UUID.fromString("11dcb1eb-54a9-47e4-9fa0-c0cddbd62177"), "Artem");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Review> reviews = List.of(
                new Review(1L, 1, product, customer,
                        LocalDateTime.parse("2024-05-16 11:22:00", formatter),
                        "advantages 1", "disadvantages 1", "comment 1"),
                new Review(2L, 2, product, customer,
                        LocalDateTime.parse("2024-05-15 12:23:00", formatter),
                        "advantages 2", "disadvantages 2", "comment 2"),
                new Review(3L, 3, product, customer,
                        LocalDateTime.parse("2024-05-16 13:24:00", formatter),
                        "advantages 3", "disadvantages 3", "comment 3"),
                new Review(4L, 4, product, customer,
                        LocalDateTime.parse("2024-05-17 14:25:00", formatter),
                        "advantages 4", "disadvantages 4", "comment 4")
        );

        // when
        Iterable<Review> result = this.reviewRepository.findAllByProductId(1L);

        // then
        assertEquals(reviews, result);
    }
}