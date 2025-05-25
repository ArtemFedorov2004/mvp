package io.github.artemfedorov2004.customerapp.controller;

import io.github.artemfedorov2004.customerapp.client.ProductsRestClient;
import io.github.artemfedorov2004.customerapp.entity.Product;
import io.github.artemfedorov2004.customerapp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductsControllerTest {

    @Mock
    ProductsRestClient productsRestClient;

    @InjectMocks
    ProductsController controller;

    @Test
    void getProductsList_ReturnsProductsListPage() {
        // given
        var model = new ConcurrentModel();

        var products = LongStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Товар №%d".formatted(i), new BigDecimal(i)))
                .toList();

        doReturn(products).when(this.productsRestClient).getAllProducts();

        // when
        var result = this.controller.getProductsList(model);

        // then
        assertEquals("online-store/products/list", result);
        assertEquals(products, model.getAttribute("products"));
    }

    @Test
    void getProduct_ProductExists_ReturnsProductPage() {
        // given
        Product product = new Product(1L, "Продукт 1", BigDecimal.valueOf(1000));
        var model = new ConcurrentModel();

        doReturn(Optional.of(product)).when(this.productsRestClient).getProduct(1L);

        // when
        var result = this.controller.getProduct(1L, model);

        // then
        assertEquals("online-store/products/product", result);

        verify(this.productsRestClient).getProduct(1L);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void getProduct_ProductDoesNotExist_ThrowsResourceNotFoundException() {
        // given
        var model = new ConcurrentModel();
        doReturn(Optional.empty()).when(this.productsRestClient).getProduct(1L);

        // when
        var exception = assertThrows(ResourceNotFoundException.class, () -> this.controller.getProduct(1L, model));

        // then
        assertEquals("online-store.errors.product.not_found", exception.getMessage());

        verify(this.productsRestClient).getProduct(1L);
        verifyNoMoreInteractions(this.productsRestClient);
    }
}
