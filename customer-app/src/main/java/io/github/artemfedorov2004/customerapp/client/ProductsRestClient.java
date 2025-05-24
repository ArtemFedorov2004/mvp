package io.github.artemfedorov2004.customerapp.client;

import io.github.artemfedorov2004.customerapp.entity.Product;

import java.util.List;

public interface ProductsRestClient {

    List<Product> getAllProducts();
}
