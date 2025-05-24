package io.github.artemfedorov2004.onlinestoreservice.repository;

import io.github.artemfedorov2004.onlinestoreservice.entity.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {
}
