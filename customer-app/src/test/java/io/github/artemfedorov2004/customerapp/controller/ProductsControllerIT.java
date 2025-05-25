package io.github.artemfedorov2004.customerapp.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.artemfedorov2004.customerapp.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
public class ProductsControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getProductList_ReturnsProductsListPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/list");

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/online-store-api/products"))
                .willReturn(WireMock.ok("""
                        [
                            {"id": 1, "title": "Товар №1", "price": 100},
                            {"id": 2, "title": "Товар №2", "price": 200}
                        ]""").withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("online-store/products/list"),
                        model().attribute("products", List.of(
                                new Product(1L, "Товар №1", new BigDecimal(100)),
                                new Product(2L, "Товар №2", new BigDecimal(200))
                        ))
                );

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/online-store-api/products")));
    }

    @Test
    void getProduct_ProductExists_ReturnsProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/1");

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/online-store-api/products/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Товар №1",
                            "price": 100
                        }
                        """).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("online-store/products/product"),
                        model().attribute("product",
                                new Product(1L, "Товар №1", new BigDecimal(100)))
                );

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/online-store-api/products/1")));
    }

    @Test
    void getProduct_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/1");

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/online-store-api/products/1"))
                .willReturn(WireMock.notFound()));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        view().name("errors/404"),
                        model().attribute("error", "Товар не найден")
                );

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/online-store-api/products/1")));
    }
}
