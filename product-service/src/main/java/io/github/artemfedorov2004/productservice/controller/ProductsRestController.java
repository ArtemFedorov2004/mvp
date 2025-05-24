package io.github.artemfedorov2004.productservice.controller;

import io.github.artemfedorov2004.productservice.entity.Product;
import io.github.artemfedorov2004.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("product-api/products")
public class ProductsRestController {

    private final ProductService productService;

    @GetMapping
    public Iterable<Product> getAllProducts() {
        return this.productService.getAllProducts();
    }
}
