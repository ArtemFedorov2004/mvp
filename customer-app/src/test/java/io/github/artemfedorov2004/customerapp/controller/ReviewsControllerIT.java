package io.github.artemfedorov2004.customerapp.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.artemfedorov2004.customerapp.controller.payload.NewReviewPayload;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
public class ReviewsControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getNewReviewPage_ProductExists_ReturnsNewReviewPage() throws Exception {
        // given
        OidcUser oidcUser = new DefaultOidcUser(
                AuthorityUtils.createAuthorityList("SCOPE_create_product_review"),
                OidcIdToken.withTokenValue("id-token")
                        .claim("preferred_username", "andrey")
                        .build(),
                "preferred_username");
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/1/reviews/create")
                .with(oidcLogin().oidcUser(oidcUser));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/online-store-api/products/1"))
                .willReturn(WireMock.ok("""
                        {
                            "id": 1,
                            "title": "Товар №1",
                            "price": 100
                        }""").withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("online-store/products/reviews/new_review"),
                        model().attribute("username", "andrey"),
                        model().attribute("productId", 1L)
                );

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/online-store-api/products/1")));
    }

    @Test
    void getNewReviewPage_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        OidcUser oidcUser = new DefaultOidcUser(
                AuthorityUtils.createAuthorityList("SCOPE_create_product_review"),
                OidcIdToken.withTokenValue("id-token")
                        .claim("preferred_username", "andrey")
                        .build(),
                "preferred_username");
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/1/reviews/create")
                .with(oidcLogin().oidcUser(oidcUser));

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

    @Test
    void getNewReviewPage_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/1/reviews/create")
                .with(user("artem"));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void createReview_RequestIsValid_ReturnsProductPage() throws Exception {
        // given
        OidcUser oidcUser = new DefaultOidcUser(
                AuthorityUtils.createAuthorityList("SCOPE_create_product_review"),
                OidcIdToken.withTokenValue("id-token")
                        .subject("11dcb1eb-54a9-47e4-9fa0-c0cddbd62177")
                        .claim("preferred_username", "andrey")
                        .build(),
                "preferred_username");
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/1/reviews/create")
                .param("rating", "1")
                .param("advantages", "advantages 1")
                .param("disadvantages", "disadvantages 1")
                .param("comment", "comment 1")
                .with(oidcLogin().oidcUser(oidcUser))
                .with(csrf());

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/online-store-api/products/1/reviews"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "rating": 1,
                            "advantages": "advantages 1",
                            "disadvantages": "disadvantages 1",
                            "comment": "comment 1"
                        }"""))
                .willReturn(WireMock.created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": 1,
                                    "createdBy": {
                                        "id": "11dcb1eb-54a9-47e4-9fa0-c0cddbd62177",
                                        "username": "andrey"
                                    },
                                    "rating": 1,
                                    "createdAt": "2024-05-16T11:22:00",
                                    "advantages": "advantages 1",
                                    "disadvantages": "disadvantages 1",
                                    "comment": "comment 1"
                                }""")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        view().name("redirect:/online-store/products/1")
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/online-store-api/products/1/reviews"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "rating": 1,
                            "advantages": "advantages 1",
                            "disadvantages": "disadvantages 1",
                            "comment": "comment 1"
                        }""")));
    }

    @Test
    void createReview_RequestIsInvalid_ReturnsNewReviewPage() throws Exception {
        // given
        OidcUser oidcUser = new DefaultOidcUser(
                AuthorityUtils.createAuthorityList("SCOPE_create_product_review"),
                OidcIdToken.withTokenValue("id-token")
                        .claim("preferred_username", "andrey")
                        .build(),
                "preferred_username");
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/1/reviews/create")
                .param("rating", "10")
                .param("advantages", "advantages 1")
                .param("disadvantages", "disadvantages 1")
                .param("comment", "comment 1")
                .with(oidcLogin().oidcUser(oidcUser))
                .with(csrf());

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/online-store-api/products/1/reviews"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "rating": 10,
                            "advantages": "advantages 1",
                            "disadvantages": "disadvantages 1",
                            "comment": "comment 1"
                        }"""))
                .willReturn(WireMock.badRequest()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                    "errors": ["Ошибка 1", "Ошибка 2"]
                                }""")));


        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("online-store/products/reviews/new_review"),
                        model().attribute("payload", new NewReviewPayload(10,
                                "advantages 1", "disadvantages 1", "comment 1")),
                        model().attribute("errors", List.of("Ошибка 1", "Ошибка 2"))
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/online-store-api/products/1/reviews"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "rating": 10,
                            "advantages": "advantages 1",
                            "disadvantages": "disadvantages 1",
                            "comment": "comment 1"
                        }""")));
    }

    @Test
    void createReview_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        OidcUser oidcUser = new DefaultOidcUser(
                AuthorityUtils.createAuthorityList("SCOPE_create_product_review"),
                OidcIdToken.withTokenValue("id-token")
                        .claim("preferred_username", "andrey")
                        .build(),
                "preferred_username");
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/1/reviews/create")
                .param("rating", "2")
                .param("advantages", "advantages 1")
                .param("disadvantages", "disadvantages 1")
                .param("comment", "comment 1")
                .with(oidcLogin().oidcUser(oidcUser))
                .with(csrf());

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/online-store-api/products/1/reviews"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "rating": 2,
                            "advantages": "advantages 1",
                            "disadvantages": "disadvantages 1",
                            "comment": "comment 1"
                        }"""))
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

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/online-store-api/products/1/reviews"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "rating": 2,
                            "advantages": "advantages 1",
                            "disadvantages": "disadvantages 1",
                            "comment": "comment 1"
                        }""")));
    }

    @Test
    void createReview_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/catalogue/products/create")
                .param("rating", "2")
                .param("advantages", "advantages 1")
                .param("disadvantages", "disadvantages 1")
                .param("comment", "comment 1")
                .with(user("artem"))
                .with(csrf());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }
}
