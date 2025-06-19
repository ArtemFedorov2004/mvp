package io.github.artemfedorov2004.managerapp.controller;

import io.github.artemfedorov2004.managerapp.client.BadRequestException;
import io.github.artemfedorov2004.managerapp.client.ProductsRestClient;
import io.github.artemfedorov2004.managerapp.controller.payload.UpdateProductPayload;
import io.github.artemfedorov2004.managerapp.entity.Product;
import io.github.artemfedorov2004.managerapp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.ConcurrentModel;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    ProductsRestClient productsRestClient;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductController controller;

    @Test
    void product_ProductExists_ReturnsProduct() {
        // given
        var product = new Product(1L, "Product 1", BigDecimal.valueOf(1));

        doReturn(Optional.of(product)).when(this.productsRestClient).getProduct(1L);

        // when
        var result = this.controller.product(1L);

        // then
        assertEquals(product, result);

        verify(this.productsRestClient).getProduct(1L);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void product_ProductDoesNotExist_ThrowsResourceNotFoundException() {
        // given

        // when
        var exception = assertThrows(ResourceNotFoundException.class, () -> this.controller.product(1L));

        // then
        assertEquals("online-store.errors.product.not_found", exception.getMessage());

        verify(this.productsRestClient).getProduct(1L);
        verifyNoMoreInteractions(this.productsRestClient);
    }

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
    void getProduct_ReturnsProductPage() {
        // given

        // when
        var result = this.controller.getProduct();

        // then
        assertEquals("online-store/products/product", result);

        verifyNoInteractions(this.productsRestClient);
    }

    @Test
    void getProductEditPage_ReturnsProductEditPage() {
        // given

        // when
        var result = this.controller.getProductEditPage();

        // then
        assertEquals("online-store/products/edit", result);

        verifyNoInteractions(this.productsRestClient);
    }

    @Test
    void updateProduct_RequestIsValid_RedirectsToProductPage() {
        // given
        var product = new Product(1L, "Product 1", BigDecimal.valueOf(100));
        var payload = new UpdateProductPayload("Product 1", BigDecimal.valueOf(100));
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        // when
        var result = this.controller.updateProduct(product, payload, model, response);

        // then
        assertEquals("redirect:/online-store/products/1", result);

        verify(this.productsRestClient).updateProduct(1L, payload);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void updateProduct_RequestIsInvalid_ReturnsProductEditPage() {
        // given
        var product = new Product(1L, "Product 1", BigDecimal.valueOf(100));
        var payload = new UpdateProductPayload("   ", BigDecimal.valueOf(-1));
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doThrow(new BadRequestException(List.of("Error 1", "Error 2")))
                .when(this.productsRestClient).updateProduct(1L, payload);

        // when
        var result = this.controller.updateProduct(product, payload, model, response);

        // then
        assertEquals("online-store/products/edit", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Error 1", "Error 2"), model.getAttribute("errors"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        verify(this.productsRestClient).updateProduct(1L, payload);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void deleteProduct_RedirectsToProductsListPage() {
        // given
        var product = new Product(1L, "Product 1", BigDecimal.valueOf(100));

        // when
        var result = this.controller.deleteProduct(product);

        // then
        assertEquals("redirect:/online-store/products/list", result);

        verify(this.productsRestClient).deleteProduct(1L);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void handleResourceNotFoundException_Returns404ErrorPage() {
        // given
        var exception = new ResourceNotFoundException("error");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();
        var locale = Locale.of("ru");

        doReturn("Error").when(this.messageSource)
                .getMessage("error", new Object[0], "error", Locale.of("ru"));

        // when
        var result = this.controller.handleResourceNotFoundException(exception, model, response, locale);

        // then
        assertEquals("errors/404", result);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        verify(this.messageSource).getMessage("error", new Object[0], "error", Locale.of("ru"));
        verifyNoMoreInteractions(this.messageSource);
        verifyNoInteractions(this.productsRestClient);
    }
}