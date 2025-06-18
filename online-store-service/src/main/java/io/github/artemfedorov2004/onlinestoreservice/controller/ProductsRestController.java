package io.github.artemfedorov2004.onlinestoreservice.controller;

import io.github.artemfedorov2004.onlinestoreservice.controller.payload.NewProductPayload;
import io.github.artemfedorov2004.onlinestoreservice.controller.payload.UpdateProductPayload;
import io.github.artemfedorov2004.onlinestoreservice.entity.Product;
import io.github.artemfedorov2004.onlinestoreservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("online-store-api/products")
public class ProductsRestController {

    private final ProductService productService;

    @GetMapping
    public Iterable<Product> getAllProducts() {
        return this.productService.getAllProducts();
    }

    @GetMapping("/{productId:\\d+}")
    public Product getProduct(@PathVariable("productId") long productId) {
        return this.productService.getProduct(productId);
    }

    @PostMapping
    @Operation(
            security = @SecurityRequirement(name = "keycloak"),
            summary = "Create a new product",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = NewProductPayload.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "title": "my product",
                                                        "price": 1220
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            headers = @Header(name = "Content-Type", description = "Тип данных"),
                            description = "Product created",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            type = "object",
                                            properties = {
                                                    @StringToClassMapItem(key = "id", value = Long.class),
                                                    @StringToClassMapItem(key = "title", value = String.class),
                                                    @StringToClassMapItem(key = "price", value = BigDecimal.class)
                                            }
                                    ),
                                    examples = {
                                            @ExampleObject(
                                                    value = """
                                                            {
                                                                "id": 3,
                                                                "title": "my product",
                                                                "price": 1220
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input"
                    )
            })
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody NewProductPayload payload,
            BindingResult bindingResult,
            UriComponentsBuilder uriComponentsBuilder) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            Product product = this.productService.createProduct(payload);
            return ResponseEntity
                    .created(uriComponentsBuilder
                            .path("/online-store-api/products/{productId}")
                            .build(Map.of("productId", product.getId())))
                    .body(product);
        }
    }

    @PatchMapping("/{productId:\\d+}")
    @Operation(
            summary = "Update product",
            security = @SecurityRequirement(name = "keycloak"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = UpdateProductPayload.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "title": "my product",
                                                        "price": 1220
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Product updated"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Product not found"
                    )
            })
    public ResponseEntity<?> updateProduct(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateProductPayload payload,
            BindingResult bindingResult) throws BindException {

        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            this.productService.updateProduct(productId, payload);
            return ResponseEntity.noContent()
                    .build();
        }
    }

    @DeleteMapping("/{productId:\\d+}")
    @Operation(
            security = @SecurityRequirement(name = "keycloak"),
            summary = "Delete product",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Product deleted"
                    )
            })
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId) {
        this.productService.deleteProduct(productId);
        return ResponseEntity.noContent()
                .build();
    }
}
