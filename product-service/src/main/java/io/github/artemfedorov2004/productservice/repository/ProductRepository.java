package io.github.artemfedorov2004.productservice.repository;

import io.github.artemfedorov2004.productservice.entity.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {
}
