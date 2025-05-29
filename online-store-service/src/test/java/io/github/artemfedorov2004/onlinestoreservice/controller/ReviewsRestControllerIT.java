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
public class ReviewsRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    @Sql("/sql/reviews.sql")
    void getAllProductReviews_ProductExists_ReturnsReviewList() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store-api/products/1/reviews");

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        content().json("""
                                [
                                    {
                                        "id": 1,
                                        "createdBy": {
                                            "id": "11dcb1eb-54a9-47e4-9fa0-c0cddbd62177",
                                            "username": "Artem"
                                         },
                                         "rating": 1,
                                         "createdAt": "2024-05-16T11:22:00",
                                         "advantages": "advantages 1",
                                         "disadvantages": "disadvantages 1",
                                         "comment": "comment 1"
                                    },
                                    {
                                        "id": 2,
                                        "createdBy": {
                                            "id": "11dcb1eb-54a9-47e4-9fa0-c0cddbd62177",
                                            "username": "Artem"
                                        },
                                        "rating": 2,
                                        "createdAt": "2024-05-15T12:23:00",
                                        "advantages": "advantages 2",
                                        "disadvantages": "disadvantages 2",
                                        "comment": "comment 2"
                                    },
                                    {
                                        "id": 3,
                                        "createdBy": {
                                            "id": "11dcb1eb-54a9-47e4-9fa0-c0cddbd62177",
                                            "username": "Artem"
                                        },
                                        "rating": 3,
                                        "createdAt": "2024-05-16T13:24:00",
                                        "advantages": "advantages 3",
                                        "disadvantages": "disadvantages 3",
                                        "comment": "comment 3"
                                    },
                                    {
                                        "id": 4,
                                        "createdBy": {
                                            "id": "11dcb1eb-54a9-47e4-9fa0-c0cddbd62177",
                                            "username": "Artem"
                                        },
                                        "rating": 4,
                                        "createdAt": "2024-05-17T14:25:00",
                                        "advantages": "advantages 4",
                                        "disadvantages": "disadvantages 4",
                                        "comment": "comment 4"
                                    }
                                ]""")
                );
    }

    @Test
    @Sql("/sql/reviews.sql")
    void getAllProductReviews_ProductDoesNotExist_ReturnsNotFound() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store-api/products/100/reviews");

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
                                    "instance": "/online-store-api/products/100/reviews"
                                }""")
                );
    }

    @Test
    @Sql("/sql/reviews.sql")
    void createReview_RequestIsValid_ReturnsNewReview() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store-api/products/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "rating": 3,
                            "advantages": "advantages",
                            "disadvantages": "disadvantages",
                            "comment": "comment"
                        }""")
                .with(jwt().jwt(builder -> builder.subject("3828cc4f-15b6-4438-815e-ac0f120c0db5")
                        .claims(claimsConsumer -> claimsConsumer.putAll(
                                Map.of("scope", "create_product_review", "preferred_username", "Artem")))));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        header().string(HttpHeaders.LOCATION, containsString("http://localhost/online-store-api/products/1/reviews/")),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        jsonPath("$.id").exists(),
                        jsonPath("$.createdAt").exists(),
                        content().json("""
                                {
                                    "createdBy": {
                                        "id": "11dcb1eb-54a9-47e4-9fa0-c0cddbd62177",
                                        "username": "Artem"
                                    },
                                    "rating": 3,
                                    "advantages": "advantages",
                                    "disadvantages": "disadvantages",
                                    "comment": "comment"
                                }"""));
    }

    @Test
    void createReview_RequestIsInvalid_ReturnsProblemDetail() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store-api/products/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "rating": 30,
                            "advantages": "advantages",
                            "disadvantages": "disadvantages",
                            "comment": "comment"
                        }""")
                .locale(Locale.of("ru", "RU"))
                .with(jwt().jwt(builder -> builder.subject("3828cc4f-15b6-4438-815e-ac0f120c0db5")
                        .claims(claimsConsumer -> claimsConsumer.putAll(
                                Map.of("scope", "create_product_review", "preferred_username", "Artem")))));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON),
                        content().json("""
                                {
                                    "type": "about:blank",
                                    "title": "Bad Request",
                                    "status": 400,
                                    "detail": "Запрос содержит ошибки",
                                    "instance": "/online-store-api/products/1/reviews",
                                    "errors": ["Рейтинг больше 5"]
                                }"""));
    }

    @Test
    void createReview_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store-api/products/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "rating": 30,
                            "advantages": "advantages",
                            "disadvantages": "disadvantages",
                            "comment": "comment"
                        }""")
                .locale(Locale.of("ru", "RU"))
                .with(jwt().jwt(builder -> builder.subject("3828cc4f-15b6-4438-815e-ac0f120c0db5")
                        .claims(claimsConsumer -> claimsConsumer.put("preferred_username", "Artem"))));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql("/sql/reviews.sql")
    void createReview_ProductWithGivenIdDoesNotExist_ReturnsNotFound() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store-api/products/10/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "rating": 3,
                            "advantages": "advantages",
                            "disadvantages": "disadvantages",
                            "comment": "comment"
                        }""")
                .locale(Locale.of("ru", "RU"))
                .with(jwt().jwt(builder -> builder.subject("3828cc4f-15b6-4438-815e-ac0f120c0db5")
                        .claims(claimsConsumer -> claimsConsumer.putAll(
                                Map.of("scope", "create_product_review", "preferred_username", "Artem")))));

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
                                    "instance": "/online-store-api/products/10/reviews"
                                }""")
                );
    }
}
