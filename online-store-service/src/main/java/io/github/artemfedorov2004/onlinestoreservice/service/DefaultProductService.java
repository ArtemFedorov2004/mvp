package io.github.artemfedorov2004.onlinestoreservice.service;

import io.github.artemfedorov2004.onlinestoreservice.controller.payload.NewProductPayload;
import io.github.artemfedorov2004.onlinestoreservice.controller.payload.UpdateProductPayload;
import io.github.artemfedorov2004.onlinestoreservice.entity.Product;
import io.github.artemfedorov2004.onlinestoreservice.exception.ResourceNotFoundException;
import io.github.artemfedorov2004.onlinestoreservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DefaultProductService implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Iterable<Product> getAllProducts() {
        return this.productRepository.findAll();
    }

    @Override
    public Product getProduct(Long productId) {
        return this.productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("online_store.errors.product.not_found"));
    }

    @Override
    public boolean existsProductById(Long productId) {
        return this.productRepository.existsById(productId);
    }

    @Override
    @Transactional
    public Product createProduct(NewProductPayload payload) {
        return this.productRepository.save(new Product(null, payload.title(), payload.price()));
    }

    @Override
    @Transactional
    public void updateProduct(Long id, UpdateProductPayload payload) {
        this.productRepository.findById(id)
                .ifPresentOrElse(product -> {
                    product.setTitle(payload.title());
                    product.setPrice(payload.price());
                }, () -> {
                    throw new ResourceNotFoundException("online_store.errors.product.not_found");
                });
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        this.productRepository.deleteById(id);
    }
}
