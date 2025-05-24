package io.github.artemfedorov2004.productservice.service;

import io.github.artemfedorov2004.productservice.entity.Product;
import io.github.artemfedorov2004.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
