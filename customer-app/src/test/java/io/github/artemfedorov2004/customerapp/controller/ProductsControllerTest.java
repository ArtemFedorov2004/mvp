package io.github.artemfedorov2004.customerapp.controller;

import io.github.artemfedorov2004.customerapp.client.ProductsRestClient;
import io.github.artemfedorov2004.customerapp.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;

import java.math.BigDecimal;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

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
}
