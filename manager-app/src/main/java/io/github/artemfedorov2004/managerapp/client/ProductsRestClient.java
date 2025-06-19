package io.github.artemfedorov2004.managerapp.client;

import io.github.artemfedorov2004.managerapp.controller.payload.NewProductPayload;
import io.github.artemfedorov2004.managerapp.controller.payload.UpdateProductPayload;
import io.github.artemfedorov2004.managerapp.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductsRestClient {

    List<Product> getAllProducts();

    Optional<Product> getProduct(Long productId);

    Product createProduct(NewProductPayload payload);

    void updateProduct(Long productId, UpdateProductPayload payload);

    void deleteProduct(Long productId);
}
