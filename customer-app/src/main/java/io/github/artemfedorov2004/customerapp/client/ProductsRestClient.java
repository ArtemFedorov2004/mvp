package io.github.artemfedorov2004.customerapp.client;

import io.github.artemfedorov2004.customerapp.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductsRestClient {

    List<Product> getAllProducts();

    Optional<Product> getProduct(long productId);
}
