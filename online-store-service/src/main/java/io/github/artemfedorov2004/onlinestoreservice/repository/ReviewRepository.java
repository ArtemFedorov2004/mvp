package io.github.artemfedorov2004.onlinestoreservice.repository;

import io.github.artemfedorov2004.onlinestoreservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query(value = "select r from Review r where r.forProduct.id = ?1")
    Iterable<Review> findAllByProductId(Long productId);
}
