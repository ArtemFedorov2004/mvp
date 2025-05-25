package io.github.artemfedorov2004.onlinestoreservice.controller;

import io.github.artemfedorov2004.onlinestoreservice.entity.Product;
import io.github.artemfedorov2004.onlinestoreservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("online-store-api/products")
public class ProductsRestController {

    private final ProductService productService;

    @GetMapping
    @Operation(security = @SecurityRequirement(name = "keycloak"))
    public Iterable<Product> getAllProducts() {
        return this.productService.getAllProducts();
    }

    @GetMapping("/{productId:\\d+}")
    @Operation(security = @SecurityRequirement(name = "keycloak"))
    public Product getProduct(@PathVariable("productId") long productId) {
        return this.productService.getProduct(productId);
    }
}
