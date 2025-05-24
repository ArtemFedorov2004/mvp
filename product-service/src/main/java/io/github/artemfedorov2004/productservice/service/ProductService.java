package io.github.artemfedorov2004.productservice.service;

import io.github.artemfedorov2004.productservice.entity.Product;

public interface ProductService {

    Iterable<Product> getAllProducts();
}
