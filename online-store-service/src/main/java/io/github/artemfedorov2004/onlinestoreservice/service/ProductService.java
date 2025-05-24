package io.github.artemfedorov2004.onlinestoreservice.service;

import io.github.artemfedorov2004.onlinestoreservice.entity.Product;

public interface ProductService {

    Iterable<Product> getAllProducts();
}
