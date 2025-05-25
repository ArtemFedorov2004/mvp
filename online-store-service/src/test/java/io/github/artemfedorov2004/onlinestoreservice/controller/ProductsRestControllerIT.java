package io.github.artemfedorov2004.onlinestoreservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ProductsRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql("/sql/products.sql")
    void getAllProducts_ReturnsProductList() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store-api/products");

        // when
        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                                [
                                    {"id": 1, "title": "Ананас", "price": 100},
                                    {"id": 2, "title": "Зефир", "price": 200},
                                    {"id": 3, "title": "Лимон", "price": 500},
                                    {"id": 4, "title": "Яблоко", "price": 1}
                                ]""")
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void getProduct_ProductExists_ReturnsProductsList() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store-api/products/1");

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "id": 1,
                                    "title": "Ананас",
                                    "price": 100
                                }""")
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void getProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store-api/products/10");

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                                {
                                    "title": "Not Found",
                                    "status": 404,
                                    "detail": "Товар не найден",
                                    "instance": "/online-store-api/products/10"
                                }""")
                );
    }
}
