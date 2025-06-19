package io.github.artemfedorov2004.managerapp.client;

import io.github.artemfedorov2004.managerapp.controller.payload.NewProductPayload;
import io.github.artemfedorov2004.managerapp.controller.payload.UpdateProductPayload;
import io.github.artemfedorov2004.managerapp.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
    public Optional<Product> getProduct(Long productId) {
        try {
            return Optional.ofNullable(this.restClient.get()
                    .uri("/online-store-api/products/{productId}", productId)
                    .retrieve()
                    .body(Product.class));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        }
    }

    @Override
    public Product createProduct(NewProductPayload payload) {
        try {
            return this.restClient
                    .post()
                    .uri("/online-store-api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Product.class);
        } catch (HttpClientErrorException.BadRequest exception) {
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException((List<String>) problemDetail.getProperties().get("errors"));
        }
    }

    @Override
    public void updateProduct(Long productId, UpdateProductPayload payload) {
        try {
            this.restClient
                    .patch()
                    .uri("/online-store-api/products/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest exception) {
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException((List<String>) problemDetail.getProperties().get("errors"));
        }
    }

    @Override
    public void deleteProduct(Long productId) {
        this.restClient
                .delete()
                .uri("/online-store-api/products/{productId}", productId)
                .retrieve()
                .toBodilessEntity();
    }
}
