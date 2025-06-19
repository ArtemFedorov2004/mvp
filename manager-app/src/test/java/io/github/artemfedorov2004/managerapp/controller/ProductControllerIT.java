package io.github.artemfedorov2004.managerapp.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.artemfedorov2004.managerapp.controller.payload.UpdateProductPayload;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WireMockTest(httpPort = 54321)
public class ProductControllerIT {

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
    void getProduct_ProductExists_ReturnsProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/1")
                .with(oidcLogin().oidcUser(this.oidcUser));

        WireMock.stubFor(WireMock.get("/online-store-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Product",
                            "price": 800
                        }
                        """)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("online-store/products/product"),
                        model().attribute("product", new Product(1L, "Product", BigDecimal.valueOf(800))),
                        model().attribute("username", "andrey")
                );
    }

    @Test
    void getProduct_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/1")
                .with(oidcLogin().oidcUser(this.oidcUser));

        WireMock.stubFor(WireMock.get("/online-store-api/products/1")
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
    }

    @Test
    void getProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/1")
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
    void getProductEditPage_ProductExists_ReturnsProductEditPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/1/edit")
                .with(oidcLogin().oidcUser(this.oidcUser));

        WireMock.stubFor(WireMock.get("/online-store-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Product",
                            "price": 900
                        }
                        """)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        view().name("online-store/products/edit"),
                        model().attribute("product", new Product(1L, "Product", BigDecimal.valueOf(900))),
                        model().attribute("username", "andrey")
                );
    }

    @Test
    void getProductEditPage_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/1/edit")
                .with(oidcLogin().oidcUser(this.oidcUser));

        WireMock.stubFor(WireMock.get("/online-store-api/products/1")
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
    }

    @Test
    void getProductEditPage_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/online-store/products/1/edit")
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
    void updateProduct_RequestIsValid_RedirectsToProductPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/1/edit")
                .param("title", "New title")
                .param("price", "777")
                .with(oidcLogin().oidcUser(this.oidcUser))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/online-store-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Title",
                            "price": 666
                        }
                        """)));

        WireMock.stubFor(WireMock.patch("/online-store-api/products/1")
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "New title",
                            "price": 777
                        }"""))
                .willReturn(WireMock.noContent()));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/online-store/products/1")
                );

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/online-store-api/products/1")));

        WireMock.verify(WireMock.patchRequestedFor(WireMock.urlPathMatching("/online-store-api/products/1"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "New title",
                            "price": 777
                        }""")));
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsProductEditPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/1/edit")
                .param("title", "   ")
                .param("price", "-1")
                .with(oidcLogin().oidcUser(this.oidcUser))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/online-store-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "   ",
                            "price": -1
                        }
                        """)));

        WireMock.stubFor(WireMock.patch("/online-store-api/products/1")
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
                        view().name("online-store/products/edit"),
                        model().attribute("product", new Product(1L, "   ", BigDecimal.valueOf(-1))),
                        model().attribute("errors", List.of("Error 1", "Error 2")),
                        model().attribute("payload", new UpdateProductPayload("   ", BigDecimal.valueOf(-1))),
                        model().attribute("username", "andrey")
                );

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathMatching("/online-store-api/products/1")));

        WireMock.verify(WireMock.patchRequestedFor(WireMock.urlPathMatching("/online-store-api/products/1"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "title": "   ",
                            "price": -1
                        }""")));
    }

    @Test
    void updateProduct_ProductDoesNotExist_ReturnsError404Page() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/1/edit")
                .param("title", "New title")
                .param("price", "777")
                .with(oidcLogin().oidcUser(this.oidcUser))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/online-store-api/products/1")
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
    }

    @Test
    void updateProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/1/edit")
                .param("title", "New title")
                .param("price", "777")
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

    @Test
    void deleteProduct_RedirectsToProductsListPage() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/1/delete")
                .with(oidcLogin().oidcUser(this.oidcUser))
                .with(csrf());

        WireMock.stubFor(WireMock.get("/online-store-api/products/1")
                .willReturn(WireMock.okJson("""
                        {
                            "id": 1,
                            "title": "Product",
                            "price": 746
                        }
                        """)));

        WireMock.stubFor(WireMock.delete("/online-store-api/products/1")
                .willReturn(WireMock.noContent()));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/online-store/products/list")
                );

        WireMock.verify(WireMock.deleteRequestedFor(WireMock.urlPathMatching("/online-store-api/products/1")));
    }

    @Test
    void deleteProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.post("/online-store/products/1/delete")
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
