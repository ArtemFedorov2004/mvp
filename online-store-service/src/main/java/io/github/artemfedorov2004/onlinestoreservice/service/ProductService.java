package io.github.artemfedorov2004.onlinestoreservice.service;

import io.github.artemfedorov2004.onlinestoreservice.controller.payload.NewProductPayload;
import io.github.artemfedorov2004.onlinestoreservice.controller.payload.UpdateProductPayload;
import io.github.artemfedorov2004.onlinestoreservice.entity.Product;

public interface ProductService {

    Iterable<Product> getAllProducts();

    Product getProduct(Long productId);

    boolean existsProductById(Long productId);

    Product createProduct(NewProductPayload payload);

    void updateProduct(Long id, UpdateProductPayload payload);

    void deleteProduct(Long id);
}
