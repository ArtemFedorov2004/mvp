package io.github.artemfedorov2004.customerapp.controller;

import io.github.artemfedorov2004.customerapp.client.ProductsRestClient;
import io.github.artemfedorov2004.customerapp.client.ReviewsRestClient;
import io.github.artemfedorov2004.customerapp.entity.Customer;
import io.github.artemfedorov2004.customerapp.entity.Product;
import io.github.artemfedorov2004.customerapp.entity.Review;
import io.github.artemfedorov2004.customerapp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.ConcurrentModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductsControllerTest {

    @Mock
    ProductsRestClient productsRestClient;

    @Mock
    ReviewsRestClient reviewsRestClient;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ProductsController controller;

    @Test
    void getProductsList_AuthenticationTokenIsNotNull_ReturnsProductsListPage() {
        // given
        var model = new ConcurrentModel();
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_view_online_store");
        OAuth2User principal = new DefaultOidcUser(
                authorities,
                OidcIdToken.withTokenValue("id-token")
                        .claim("preferred_username", "andrey")
                        .build(),
                "preferred_username");
        OAuth2AuthenticationToken authenticationToken = new OAuth2AuthenticationToken(principal, authorities, "keycloak");

        var products = LongStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Товар №%d".formatted(i), new BigDecimal(i)))
                .toList();

        doReturn(products).when(this.productsRestClient).getAllProducts();

        // when
        var result = this.controller.getProductsList(model, authenticationToken);

        // then
        assertEquals("online-store/products/list", result);
        assertEquals(products, model.getAttribute("products"));
        assertEquals("andrey", model.getAttribute("username"));
    }

    @Test
    void getProductsList_AuthenticationTokenIsNull_ReturnsProductsListPage() {
        // given
        var model = new ConcurrentModel();
        var products = LongStream.range(1, 4)
                .mapToObj(i -> new Product(i, "Товар №%d".formatted(i), new BigDecimal(i)))
                .toList();

        doReturn(products).when(this.productsRestClient).getAllProducts();

        // when
        var result = this.controller.getProductsList(model, null);

        // then
        assertEquals("online-store/products/list", result);
        assertEquals(products, model.getAttribute("products"));
        assertFalse(model.containsAttribute("username"));
    }

    @Test
    void getProduct_ProductExists_ReturnsProductPage() {
        // given
        Product product = new Product(1L, "Продукт 1", BigDecimal.valueOf(1000));
        List<Review> reviews = List.of(
                new Review(1L, new Customer(UUID.randomUUID(), "andrey"), 5,
                        LocalDateTime.now(), "advantages", "disadvantages", "comment"),
                new Review(2L, new Customer(UUID.randomUUID(), "andrey"), 3,
                        LocalDateTime.now(), "advantages", "disadvantages", "comment")
        );
        var model = new ConcurrentModel();
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_view_online_store");
        OAuth2User principal = new DefaultOidcUser(
                authorities,
                OidcIdToken.withTokenValue("id-token")
                        .claim("preferred_username", "andrey")
                        .build(),
                "preferred_username");
        OAuth2AuthenticationToken authenticationToken = new OAuth2AuthenticationToken(principal, authorities, "keycloak");

        doReturn(Optional.of(product)).when(this.productsRestClient).getProduct(1L);
        doReturn(reviews).when(this.reviewsRestClient).getAllProductReviews(1L);

        // when
        var result = this.controller.getProduct(1L, model, authenticationToken);

        // then
        assertEquals("online-store/products/product", result);
        assertEquals(product, model.getAttribute("product"));
        assertEquals(reviews, model.getAttribute("reviews"));
        assertEquals("andrey", model.getAttribute("username"));

        verify(this.productsRestClient).getProduct(1L);
        verifyNoMoreInteractions(this.productsRestClient);

        verify(this.reviewsRestClient).getAllProductReviews(1L);
        verifyNoMoreInteractions(this.reviewsRestClient);
    }

    @Test
    void getProduct_ProductExistsAndAuthenticationTokenIsNull_ReturnsProductPage() {
        // given
        Product product = new Product(1L, "Продукт 1", BigDecimal.valueOf(1000));
        List<Review> reviews = List.of(
                new Review(1L, new Customer(UUID.randomUUID(), "andrey"), 5,
                        LocalDateTime.now(), "advantages", "disadvantages", "comment"),
                new Review(2L, new Customer(UUID.randomUUID(), "andrey"), 3,
                        LocalDateTime.now(), "advantages", "disadvantages", "comment")
        );
        var model = new ConcurrentModel();

        doReturn(Optional.of(product)).when(this.productsRestClient).getProduct(1L);
        doReturn(reviews).when(this.reviewsRestClient).getAllProductReviews(1L);

        // when
        var result = this.controller.getProduct(1L, model, null);

        // then
        assertEquals("online-store/products/product", result);
        assertEquals(product, model.getAttribute("product"));
        assertEquals(reviews, model.getAttribute("reviews"));
        assertFalse(model.containsAttribute("username"));

        verify(this.productsRestClient).getProduct(1L);
        verifyNoMoreInteractions(this.productsRestClient);

        verify(this.reviewsRestClient).getAllProductReviews(1L);
        verifyNoMoreInteractions(this.reviewsRestClient);
    }

    @Test
    void getProduct_ProductDoesNotExist_ThrowsResourceNotFoundException() {
        // given
        var model = new ConcurrentModel();
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_view_online_store");
        OAuth2User principal = new DefaultOidcUser(
                authorities,
                OidcIdToken.withTokenValue("id-token")
                        .claim("preferred_username", "andrey")
                        .build(),
                "preferred_username");
        OAuth2AuthenticationToken authenticationToken = new OAuth2AuthenticationToken(principal, authorities, "keycloak");

        doReturn(Optional.empty()).when(this.productsRestClient).getProduct(1L);

        // when
        var exception = assertThrows(ResourceNotFoundException.class, () -> this.controller.getProduct(1L, model, authenticationToken));

        // then
        assertEquals("online-store.errors.product.not_found", exception.getMessage());

        verify(this.productsRestClient).getProduct(1L);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void handleResourceNotFoundException_Returns404ErrorPage() {
        // given
        var exception = new ResourceNotFoundException("error");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();
        var locale = Locale.of("ru");

        doReturn("Ошибка").when(this.messageSource)
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
