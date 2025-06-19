package io.github.artemfedorov2004.managerapp.controller;

import io.github.artemfedorov2004.managerapp.client.BadRequestException;
import io.github.artemfedorov2004.managerapp.client.ProductsRestClient;
import io.github.artemfedorov2004.managerapp.controller.payload.NewProductPayload;
import io.github.artemfedorov2004.managerapp.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.ConcurrentModel;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsControllerTest {

    @Mock
    ProductsRestClient productsRestClient;

    @InjectMocks
    ProductsController controller;

    @Test
    void username_ReturnsPreferredUsername() {
        // given
        Map<String, Object> attributes = Map.of("preferred_username", "testuser");
        OAuth2User principal = new DefaultOAuth2User(
                Collections.emptyList(),
                attributes,
                "preferred_username");

        OAuth2AuthenticationToken authenticationToken = new OAuth2AuthenticationToken(principal, Collections.emptyList(), "registrationId");

        // when
        String result = controller.username(authenticationToken);

        // then
        assertEquals("testuser", result);
    }

    @Test
    void getProductsList_ReturnsProductsListPage() {
        // given
        var model = new ConcurrentModel();

        var products = LongStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Product №%d".formatted(i),
                        BigDecimal.valueOf(i * 100)))
                .toList();

        doReturn(products).when(this.productsRestClient).getAllProducts();

        // when
        var result = this.controller.getProductsList(model);

        // then
        assertEquals("online-store/products/list", result);
        assertEquals(products, model.getAttribute("products"));
    }

    @Test
    void getNewProductPage_ReturnsNewProductPage() {
        // given

        // when
        var result = this.controller.getNewProductPage();

        // then
        assertEquals("online-store/products/new_product", result);
    }

    @Test
    void createProduct_RequestIsValid_ReturnsRedirectionToProductPage() {
        // given
        var payload = new NewProductPayload("New product", BigDecimal.valueOf(100));
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doReturn(new Product(1L, "New product", BigDecimal.valueOf(100)))
                .when(this.productsRestClient)
                .createProduct(payload);

        // when
        var result = this.controller.createProduct(payload, model, response);

        // then
        assertEquals("redirect:/online-store/products/1", result);

        verify(this.productsRestClient).createProduct(payload);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void createProduct_RequestIsInvalid_ReturnsProductFormWithErrors() {
        // given
        var payload = new NewProductPayload("  ", BigDecimal.valueOf(-1));
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doThrow(new BadRequestException(List.of("Error 1", "Error 2")))
                .when(this.productsRestClient)
                .createProduct(payload);

        // when
        var result = this.controller.createProduct(payload, model, response);

        // then
        assertEquals("online-store/products/new_product", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Error 1", "Error 2"), model.getAttribute("errors"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        verify(this.productsRestClient).createProduct(payload);
        verifyNoMoreInteractions(this.productsRestClient);
    }
}
