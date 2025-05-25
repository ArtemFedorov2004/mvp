package io.github.artemfedorov2004.customerapp.client;

import io.github.artemfedorov2004.customerapp.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultProductsRestClient implements ProductsRestClient {

    private static final ParameterizedTypeReference<List<Product>> PRODUCTS_TYPE_REFERENCE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    @Override
    public List<Product> getAllProducts() {
        return this.restClient
                .get()
                .uri("/online-store-api/products")
                .retrieve()
                .body(PRODUCTS_TYPE_REFERENCE);
    }

    @Override
    public Optional<Product> getProduct(long productId) {
        try {
            return Optional.ofNullable(this.restClient.get()
                    .uri("/online-store-api/products/{productId}", productId)
                    .retrieve()
                    .body(Product.class));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        }
    }
}
