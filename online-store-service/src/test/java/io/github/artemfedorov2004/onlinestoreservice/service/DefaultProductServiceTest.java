package io.github.artemfedorov2004.onlinestoreservice.service;

import io.github.artemfedorov2004.onlinestoreservice.entity.Product;
import io.github.artemfedorov2004.onlinestoreservice.exception.ResourceNotFoundException;
import io.github.artemfedorov2004.onlinestoreservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    DefaultProductService service;

    @Test
    void getAllProducts_ReturnsProductList() {
        // given
        List<Product> products = LongStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Продукт №%d".formatted(i), new BigDecimal(100 * i)))
                .toList();

        doReturn(products).when(this.productRepository).findAll();

        // when
        Iterable<Product> result = this.service.getAllProducts();

        // then
        assertEquals(products, result);

        verify(this.productRepository).findAll();
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void getProduct_ProductExists_ReturnsExistingProduct() {
        // given
        Product product = new Product(1L, "Товар №1", new BigDecimal(1000));

        doReturn(Optional.of(product)).when(this.productRepository).findById(1L);

        // when
        var result = this.service.getProduct(1L);

        // then
        assertNotNull(result);
        assertEquals(product, result);

        verify(this.productRepository).findById(1L);
        verifyNoMoreInteractions(this.productRepository);
    }

    @Test
    void getProduct_ProductDoesNotExist_ThrowsResourceNotFoundException() {
        // given
        doReturn(Optional.empty()).when(this.productRepository).findById(10L);

        // when
        assertThrows(ResourceNotFoundException.class, () -> this.service
                .getProduct(10L));

        // then
        verify(this.productRepository).findById(10L);
        verifyNoMoreInteractions(this.productRepository);
    }
}
