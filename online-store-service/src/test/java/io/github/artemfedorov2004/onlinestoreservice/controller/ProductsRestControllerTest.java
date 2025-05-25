package io.github.artemfedorov2004.onlinestoreservice.controller;

import io.github.artemfedorov2004.onlinestoreservice.entity.Product;
import io.github.artemfedorov2004.onlinestoreservice.exception.ResourceNotFoundException;
import io.github.artemfedorov2004.onlinestoreservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.LongStream;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
