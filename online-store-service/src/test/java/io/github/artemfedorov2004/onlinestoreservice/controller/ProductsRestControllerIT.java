package io.github.artemfedorov2004.onlinestoreservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    void createProduct_RequestIsValid_ReturnsNewProduct() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Конфета",
                            "price": 2000
                        }""")
                .with(jwt().jwt(builder -> builder.subject("3828cc4f-15b6-4438-815e-ac0f120c0db5")
                        .claims(claimsConsumer -> claimsConsumer.putAll(
                                Map.of("scope", "edit_products", "preferred_username", "Artem")))));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, containsString("http://localhost/online-store-api/products/")),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "title": "Конфета",
                                    "price": 2000
                                }"""),
                        jsonPath("$.id").exists());
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsProblemDetail() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "   ",
                            "price": -1
                        }""")
                .locale(Locale.of("ru", "RU"))
                .with(jwt().jwt(builder -> builder.subject("3828cc4f-15b6-4438-815e-ac0f120c0db5")
                        .claims(claimsConsumer -> claimsConsumer.putAll(
                                Map.of("scope", "edit_products", "preferred_username", "Artem")))));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                                {
                                    "errors": [
                                        "Цена товара должна быть положительным числом",
                                        "Название товара должно быть указано"
                                    ]
                                }"""));
    }

    @Test
    void createProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store-api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Конфета",
                            "price": 2000
                        }""")
                .locale(Locale.of("ru", "RU"))
                .with(jwt());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void updateProduct_RequestIsValid_ReturnsNoContent() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.patch("/online-store-api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Ананас Премиум",
                            "price": 150.50
                        }""")
                .with(jwt().jwt(builder -> builder.subject("3828cc4f-15b6-4438-815e-ac0f120c0db5")
                        .claims(claimsConsumer -> claimsConsumer.putAll(
                                Map.of("scope", "edit_products", "preferred_username", "Artem")))));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void updateProduct_RequestIsInvalid_ReturnsBadRequest() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.patch("/online-store-api/products/1")
                .locale(Locale.of("ru"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "   ",
                            "price": -1
                        }""")
                .with(jwt().jwt(builder -> builder.subject("3828cc4f-15b6-4438-815e-ac0f120c0db5")
                        .claims(claimsConsumer -> claimsConsumer.putAll(
                                Map.of("scope", "edit_products", "preferred_username", "Artem")))));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                                {
                                    "errors": [
                                        "Название товара должно быть указано",
                                        "Цена товара должна быть положительным числом"
                                    ]
                                }""")
                );
    }

    @Test
    void updateProduct_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.patch("/online-store-api/products/543")
                .locale(Locale.of("ru"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Несуществующий товар",
                            "price": 100
                        }""")
                .with(jwt().jwt(builder -> builder.subject("3828cc4f-15b6-4438-815e-ac0f120c0db5")
                        .claims(claimsConsumer -> claimsConsumer.putAll(
                                Map.of("scope", "edit_products", "preferred_username", "Artem")))));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNotFound()
                );
    }

    @Test
    void updateProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.patch("/online-store-api/products/1")
                .locale(Locale.of("ru"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Новое название",
                            "price": 100
                        }""")
                .with(jwt());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql("/sql/products.sql")
    void deleteProduct_ReturnsNoContent() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.delete("/online-store-api/products/1")
                .with(jwt().jwt(builder -> builder.subject("3828cc4f-15b6-4438-815e-ac0f120c0db5")
                        .claims(claimsConsumer -> claimsConsumer.putAll(
                                Map.of("scope", "edit_products", "preferred_username", "Artem")))));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    void deleteProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.delete("/online-store-api/products/1")
                .with(jwt());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }
}
