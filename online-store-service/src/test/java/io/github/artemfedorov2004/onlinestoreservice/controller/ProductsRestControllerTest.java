package io.github.artemfedorov2004.onlinestoreservice.controller;

import io.github.artemfedorov2004.onlinestoreservice.controller.payload.NewProductPayload;
import io.github.artemfedorov2004.onlinestoreservice.controller.payload.UpdateProductPayload;
import io.github.artemfedorov2004.onlinestoreservice.entity.Product;
import io.github.artemfedorov2004.onlinestoreservice.exception.ResourceNotFoundException;
import io.github.artemfedorov2004.onlinestoreservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductsRestControllerTest {

    @Mock
    ProductService productService;

    @InjectMocks
    ProductsRestController controller;

    @Test
    void getAllProducts_ReturnsProductsList() {
        // given
        List<Product> products = LongStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Продукт №%d".formatted(i), new BigDecimal(100 * i)))
                .toList();

        doReturn(products).when(this.productService).getAllProducts();

        // when
        Iterable<Product> result = this.controller.getAllProducts();

        // then
        assertEquals(products, result);

        verify(this.productService).getAllProducts();
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void getProduct_ProductExists_ReturnsProduct() {
        // given
        Product product = new Product(1L, "Продукт 1", new BigDecimal(200));

        doReturn(product).when(this.productService).getProduct(1L);

        // when
        var result = this.controller.getProduct(1L);

        // then
        assertEquals(product, result);
    }

    @Test
    void getProduct_ProductDoesNotExist_ThrowsResourceNotFoundException() {
        // given
        doThrow(new ResourceNotFoundException("online_store.errors.product.not_found")).when(this.productService).getProduct(1L);

        // when
        var exception = assertThrows(ResourceNotFoundException.class, () -> this.controller.getProduct(1L));

        // then
        assertEquals("online_store.errors.product.not_found", exception.getMessage());
    }

    @Test
    void createProduct_RequestIsValid_ReturnsCreated() throws BindException {
        // given
        var payload = new NewProductPayload("Товар 1", BigDecimal.valueOf(66));
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        doReturn(new Product(1L, "Товар 1", BigDecimal.valueOf(66)))
                .when(this.productService).createProduct(payload);

        // when
        var result = this.controller.createProduct(payload, bindingResult, uriComponentsBuilder);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(URI.create("http://localhost/online-store-api/products/1"),
                result.getHeaders().getLocation());
        assertEquals(new Product(1L, "Товар 1", BigDecimal.valueOf(66)), result.getBody());

        verify(this.productService).createProduct(payload);
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void createProduct_RequestIsInvalid_ThrowsBindException() {
        // given
        var payload = new NewProductPayload("   ", null);
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "title is blank"));
        bindingResult.addError(new FieldError("payload", "price", "price is null"));
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // when
        var exception = assertThrows(BindException.class,
                () -> this.controller.createProduct(payload, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "title is blank"),
                new FieldError("payload", "price", "price is null")), exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void createProduct_RequestIsInvalidAndBindingResultIsBindException_ThrowsOriginalException() {
        // given
        var payload = new NewProductPayload("   ", null);
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "title is blank"));
        bindingResult.addError(new FieldError("payload", "price", "price is null"));
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // when
        var exception = assertThrows(BindException.class,
                () -> this.controller.createProduct(payload, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "title is blank"),
                new FieldError("payload", "price", "price is null")), exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void updateProduct_RequestIsValid_ReturnsNoContent() throws BindException {
        // given
        var payload = new UpdateProductPayload("Арбуз", BigDecimal.valueOf(100));
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        // when
        var result = this.controller.updateProduct(1L, payload, bindingResult);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.productService).updateProduct(1L, payload);
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void updateProduct_RequestIsInvalid_ThrowsBindException() {
        // given
        var payload = new UpdateProductPayload("   ", BigDecimal.valueOf(-1));
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "title", "The title cannot be blank"));
        bindingResult.addError(new FieldError("payload", "price", "The price must be a positive number"));

        // when
        var exception = assertThrows(BindException.class,
                () -> this.controller.updateProduct(1L, payload, bindingResult));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "The title cannot be blank"),
                new FieldError("payload", "price", "The price must be a positive number")), exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void updateProduct_RequestIsInvalidAndBindResultIsBindException_ThrowsOriginalException() {
        // given
        var payload = new UpdateProductPayload("   ", BigDecimal.valueOf(-1));
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "title", "The title cannot be blank"));
        bindingResult.addError(new FieldError("payload", "price", "The price must be a positive number"));

        // when
        var exception = assertThrows(BindException.class,
                () -> this.controller.updateProduct(1L, payload, bindingResult));

        // then
        assertEquals(List.of(new FieldError("payload", "title", "The title cannot be blank"),
                new FieldError("payload", "price", "The price must be a positive number")), exception.getAllErrors());
        verifyNoInteractions(this.productService);
    }

    @Test
    void updateProduct_ProductNotFound_ThrowsResourceNotFoundException() {
        // given
        var payload = new UpdateProductPayload("Несуществующий товар", BigDecimal.valueOf(1));
        var bindingResult = new MapBindingResult(Map.of(), "payload");

        doThrow(new ResourceNotFoundException("online_store.errors.product.not_found"))
                .when(this.productService).updateProduct(100L, payload);

        // when
        assertThrows(ResourceNotFoundException.class,
                () -> this.controller.updateProduct(100L, payload, bindingResult));

        // then
        verify(this.productService).updateProduct(100L, payload);
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void deleteProduct_ReturnsNoContent() {
        // given

        // when
        var result = this.controller.deleteProduct(1L);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        verify(this.productService).deleteProduct(1L);
    }
}
