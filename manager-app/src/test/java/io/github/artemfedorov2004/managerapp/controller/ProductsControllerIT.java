package io.github.artemfedorov2004.managerapp.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.artemfedorov2004.managerapp.controller.payload.NewProductPayload;
import io.github.artemfedorov2004.managerapp.entity.Product;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
public class ProductsControllerIT {

    @Autowired
    MockMvc mockMvc;

    OidcUser oidcUser;

    @BeforeEach
    void setUp() {
        this.oidcUser = new DefaultOidcUser(
                AuthorityUtils.createAuthorityList("ROLE_MANAGER"),
                OidcIdToken.withTokenValue("id-token")
                        .claim("preferred_username", "andrey")
                        .build(),
                "preferred_username");
    }

    @Test
    void getProductList_ReturnsProductsListPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/list")
                .with(oidcLogin().oidcUser(this.oidcUser));

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/online-store-api/products"))
                .willReturn(WireMock.ok("""
                        [
                            {"id": 1, "title": "Product 1", "price": 100},
                            {"id": 2, "title": "Product 2", "price": 200}
                        ]""").withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("online-store/products/list"),
                        model().attribute("products", List.of(
                                new Product(1L, "Product 1", BigDecimal.valueOf(100)),
                                new Product(2L, "Product 2", BigDecimal.valueOf(200))
                        )),
                        model().attribute("username", "andrey")
                );

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/online-store-api/products")));
    }

    @Test
    void getProductList_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/list")
                .with(oidcLogin());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void getNewProductPage_ReturnsProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/create")
                .with(oidcLogin().oidcUser(this.oidcUser));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("online-store/products/new_product"),
                        model().attribute("username", "andrey")
                );
    }

    @Test
    void getNewProductPage_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/create")
                .with(oidcLogin());

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void createProduct_RequestIsValid_RedirectsToProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/create")
                .param("title", "New product")
                .param("price", "500")
                .with(oidcLogin().oidcUser(this.oidcUser))
                .with(csrf());

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/online-store-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "New product",
                            "price": 500
                        }"""))
                .willReturn(WireMock.created()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "id": 1,
                                    "title": "New product",
                                    "price": 500
                                }""")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        header().string(HttpHeaders.LOCATION, "/online-store/products/1")
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/online-store-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "New product",
                            "price": 500
                        }""")));
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsNewProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/create")
                .param("title", "   ")
                .param("price", "-1")
                .with(oidcLogin().oidcUser(this.oidcUser))
                .with(csrf());

        WireMock.stubFor(WireMock.post(WireMock.urlPathMatching("/online-store-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "   ",
                            "price": -1
                        }"""))
                .willReturn(WireMock.badRequest()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                        .withBody("""
                                {
                                    "errors": ["Error 1", "Error 2"]
                                }""")));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        view().name("online-store/products/new_product"),
                        model().attribute("username", "andrey"),
                        model().attribute("payload", new NewProductPayload("   ", BigDecimal.valueOf(-1))),
                        model().attribute("errors", List.of("Error 1", "Error 2"))
                );

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlPathMatching("/online-store-api/products"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "   ",
                            "price": -1
                        }""")));
    }

    @Test
    void createProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/create")
                .param("title", "New product")
                .param("price", "999")
                .with(oidcLogin())
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
