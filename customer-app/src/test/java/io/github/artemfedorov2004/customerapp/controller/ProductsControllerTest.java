package io.github.artemfedorov2004.customerapp.controller;

import io.github.artemfedorov2004.customerapp.client.ProductsRestClient;
import io.github.artemfedorov2004.customerapp.entity.Product;
import io.github.artemfedorov2004.customerapp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.ConcurrentModel;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductsControllerTest {

    @Mock
    ProductsRestClient productsRestClient;

    @InjectMocks
    ProductsController controller;

    @Test
    void getProductsList_ReturnsProductsListPage() {
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
    void getProduct_ProductExists_ReturnsProductPage() {
        // given
        Product product = new Product(1L, "Продукт 1", BigDecimal.valueOf(1000));
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

        // when
        var result = this.controller.getProduct(1L, model, authenticationToken);

        // then
        assertEquals("online-store/products/product", result);

        verify(this.productsRestClient).getProduct(1L);
        verifyNoMoreInteractions(this.productsRestClient);
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
}
