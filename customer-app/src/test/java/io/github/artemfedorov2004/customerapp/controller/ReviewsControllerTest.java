package io.github.artemfedorov2004.customerapp.controller;

import io.github.artemfedorov2004.customerapp.client.BadRequestException;
import io.github.artemfedorov2004.customerapp.client.ProductsRestClient;
import io.github.artemfedorov2004.customerapp.client.ReviewsRestClient;
import io.github.artemfedorov2004.customerapp.controller.payload.NewReviewPayload;
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
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewsControllerTest {

    @Mock
    ProductsRestClient productsRestClient;

    @Mock
    ReviewsRestClient reviewsRestClient;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    ReviewsController controller;

    @Test
    void getNewReviewPage_ProductExists_ReturnsNewReviewPage() {
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

        doReturn(Optional.of(new Product(1L, "title", BigDecimal.valueOf(100))))
                .when(this.productsRestClient).getProduct(1);

        // when
        var result = this.controller.getNewReviewPage(1, model, authenticationToken);

        // then
        assertEquals("online-store/products/reviews/new_review", result);
        assertEquals(1L, model.getAttribute("productId"));
        assertEquals("andrey", model.getAttribute("username"));

        verify(this.productsRestClient).getProduct(1L);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void getNewReviewPage_ProductDoesNotExist_ThrowsResourceNotFoundException() {
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
        var exception = assertThrows(ResourceNotFoundException.class, () -> this.controller.getNewReviewPage(1L, model, authenticationToken));

        // then
        assertEquals("online-store.errors.product.not_found", exception.getMessage());

        verify(this.productsRestClient).getProduct(1L);
        verifyNoMoreInteractions(this.productsRestClient);
    }

    @Test
    void createReview_RequestIsValid_ReturnsRedirectionToProductPage() {
        // given
        var payload = new NewReviewPayload(4, "advantages", "disadvantages", "comment");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doReturn(new Review(1L, new Customer(UUID.randomUUID(), "artem"), 4,
                LocalDateTime.now(), "advantages", "disadvantages", "comment"))
                .when(this.reviewsRestClient)
                .createReview(1, payload);

        // when
        var result = this.controller.createReview(1L, payload, model, response);

        // then
        assertEquals("redirect:/online-store/products/1", result);

        verify(this.reviewsRestClient).createReview(1, payload);
        verifyNoMoreInteractions(this.reviewsRestClient);
    }

    @Test
    void createReview_RequestIsInvalid_ReturnsNewReviewFormWithErrors() {
        // given
        var payload = new NewReviewPayload(null, "advantages", "disadvantages", "comment");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doThrow(new BadRequestException(List.of("Ошибка 1", "Ошибка 2")))
                .when(this.reviewsRestClient)
                .createReview(1L, payload);

        // when
        var result = this.controller.createReview(1L, payload, model, response);

        // then
        assertEquals("online-store/products/reviews/new_review", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        verify(this.reviewsRestClient).createReview(1L, payload);
        verifyNoMoreInteractions(this.reviewsRestClient);
    }

    @Test
    void createReview_ProductDoesNotExist_ThrowsResourceNotFoundException() {
        // given
        var payload = new NewReviewPayload(null, "advantages", "disadvantages", "comment");
        var model = new ConcurrentModel();
        var response = new MockHttpServletResponse();

        doThrow(HttpClientErrorException.NotFound.class)
                .when(this.reviewsRestClient)
                .createReview(1L, payload);

        // when
        var exception = assertThrows(ResourceNotFoundException.class, () ->
                this.controller.createReview(1L, payload, model, response));

        // then
        assertEquals("online-store.errors.product.not_found", exception.getMessage());

        verify(this.reviewsRestClient).createReview(1L, payload);
        verifyNoMoreInteractions(this.reviewsRestClient);
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
        verifyNoInteractions(this.reviewsRestClient);
    }
}