package io.github.artemfedorov2004.productservice.service;

import io.github.artemfedorov2004.productservice.entity.Product;
import io.github.artemfedorov2004.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultProductService implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Iterable<Product> getAllProducts() {
        return this.productRepository.findAll();
    }
}
