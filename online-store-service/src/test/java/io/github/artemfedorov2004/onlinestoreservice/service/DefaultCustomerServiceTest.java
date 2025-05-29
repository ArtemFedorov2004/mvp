package io.github.artemfedorov2004.onlinestoreservice.service;

import io.github.artemfedorov2004.onlinestoreservice.entity.Customer;
import io.github.artemfedorov2004.onlinestoreservice.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCustomerServiceTest {

    @Mock
    CustomerRepository customerRepository;

    @InjectMocks
    DefaultCustomerService service;

    @Test
    void syncCustomerAndOidcUser_CustomerWithGivenOidcUserIdIsNotExist_SaveCustomer() {
        // given
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                new Jwt("token", Instant.now(), Instant.MAX,
                        Map.of("header", new Object()), Map.of("preferred_username", "Artem")),
                null, "7bd41e94-f9f7-4244-90c9-1eced9671ef9");

        doReturn(Optional.empty())
                .when(this.customerRepository).findCustomerIdByOidcUserId(
                        UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
                );

        Customer customer = new Customer(null, "Artem");
        Customer saved = new Customer(UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061"), "Artem");
        doReturn(saved).when(this.customerRepository).save(customer);

        doNothing().when(this.customerRepository).linkCustomerIdAndOidcUserId(
                UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061"),
                UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
        );

        // when
        this.service.syncCustomerAndOidcUser(token);

        // then
        verify(this.customerRepository).save(customer);
        verify(this.customerRepository).linkCustomerIdAndOidcUserId(
                UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061"),
                UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
        );
    }

    @Test
    void syncCustomerAndOidcUser_GivenOidcUserIdExistsInDatabaseAndCustomerWithGivenIdDoesNotExist_ThrowsRuntimeException() {
        // given
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                new Jwt("token", Instant.now(), Instant.MAX,
                        Map.of("header", new Object()), Map.of("preferred_username", "Artem")),
                null, "7bd41e94-f9f7-4244-90c9-1eced9671ef9");

        doReturn(Optional.of(UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061")))
                .when(this.customerRepository).findCustomerIdByOidcUserId(
                        UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
                );

        doThrow(new RuntimeException("Relation t_customer does not contain customer with id - bd3c12bd-9ec9-44d9-8c60-b035dbcfd061"))
                .when(this.customerRepository).findById(
                        UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061"));

        // when
        var exception = assertThrows(RuntimeException.class, () -> this.service.syncCustomerAndOidcUser(token));

        // then
        assertEquals("Relation t_customer does not contain customer with id - bd3c12bd-9ec9-44d9-8c60-b035dbcfd061",
                exception.getMessage());

        verify(this.customerRepository).findCustomerIdByOidcUserId(
                UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
        );
        verify(this.customerRepository).findById(
                UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061")
        );
        verifyNoMoreInteractions(this.customerRepository);
    }

    @Test
    void syncCustomerAndOidcUser_GivenOidcUserIdExistsInDatabaseAndCustomerWithGivenIdExists_UpdatesCustomerName() {
        // given
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                new Jwt("token", Instant.now(), Instant.MAX,
                        Map.of("header", new Object()), Map.of("preferred_username", "Artem")),
                null, "7bd41e94-f9f7-4244-90c9-1eced9671ef9");

        doReturn(Optional.of(UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061")))
                .when(this.customerRepository).findCustomerIdByOidcUserId(
                        UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
                );

        Customer customer = new Customer(UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061"), "Artem");
        doReturn(Optional.of(customer)).when(this.customerRepository).findById(
                UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061")
        );

        // when
        this.service.syncCustomerAndOidcUser(token);

        // then
        verify(this.customerRepository).findCustomerIdByOidcUserId(
                UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
        );
        verify(this.customerRepository).findById(
                UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061")
        );
        verifyNoMoreInteractions(this.customerRepository);
    }

    @Test
    void getCurrentCustomer_CustomerIsNotAuthenticated_ReturnsNull() {
        // given
        var authentication = new AnonymousAuthenticationToken("key", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        Customer result = this.service.getCurrentCustomer();

        // then
        assertNull(result);
    }

    @Test
    void getCurrentCustomer_CustomerIsAuthenticatedAndCustomerWithOidcIdExists_ReturnCustomer() {
        // given
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                new Jwt("token", Instant.now(), Instant.MAX,
                        Map.of("header", new Object()), Map.of("claim", new Object())),
                null, "7bd41e94-f9f7-4244-90c9-1eced9671ef9");
        SecurityContextHolder.getContext().setAuthentication(token);

        doReturn(Optional.of(UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061")))
                .when(this.customerRepository).findCustomerIdByOidcUserId(
                        UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
                );

        Customer customer = new Customer(UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061"), "Artem");
        doReturn(Optional.of(customer)).when(this.customerRepository).findById(
                UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061")
        );

        // when
        Customer result = this.service.getCurrentCustomer();

        // then
        assertEquals(customer, result);
    }

    @Test
    void getCurrentCustomer_CustomerIsAuthenticatedAndCustomerWithGivenIdDoesNotExist_ThrowsRuntimeException() {
        // given
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                new Jwt("token", Instant.now(), Instant.MAX,
                        Map.of("header", new Object()), Map.of("claim", new Object())),
                null, "7bd41e94-f9f7-4244-90c9-1eced9671ef9");
        SecurityContextHolder.getContext().setAuthentication(token);

        doReturn(Optional.of(UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061")))
                .when(this.customerRepository).findCustomerIdByOidcUserId(
                        UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
                );


        doReturn(Optional.empty()).when(this.customerRepository).findById(
                UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061")
        );

        // when
        var exception = assertThrows(RuntimeException.class, () -> this.service.getCurrentCustomer());

        // then
        assertEquals("Relation t_customer does not contain customer with id - " +
                "bd3c12bd-9ec9-44d9-8c60-b035dbcfd061", exception.getMessage());

        verify(this.customerRepository).findCustomerIdByOidcUserId(
                UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
        );
        verify(this.customerRepository).findById(
                UUID.fromString("bd3c12bd-9ec9-44d9-8c60-b035dbcfd061")
        );
        verifyNoMoreInteractions(this.customerRepository);
    }

    @Test
    void getCurrentCustomer_CustomerIsAuthenticatedAndCustomerWithGivenOidcUserIdDoesNotExist_ThrowsRuntimeException() {
        // given
        JwtAuthenticationToken token = new JwtAuthenticationToken(
                new Jwt("token", Instant.now(), Instant.MAX,
                        Map.of("header", new Object()), Map.of("claim", new Object())),
                null, "7bd41e94-f9f7-4244-90c9-1eced9671ef9");
        SecurityContextHolder.getContext().setAuthentication(token);

        doReturn(Optional.empty())
                .when(this.customerRepository).findCustomerIdByOidcUserId(
                        UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
                );

        // when
        var exception = assertThrows(RuntimeException.class, () -> this.service.getCurrentCustomer());

        // then
        assertEquals("Customer with given oidcUserId " +
                "- 7bd41e94-f9f7-4244-90c9-1eced9671ef9 not found", exception.getMessage());

        verify(this.customerRepository).findCustomerIdByOidcUserId(
                UUID.fromString("7bd41e94-f9f7-4244-90c9-1eced9671ef9")
        );
        verifyNoMoreInteractions(this.customerRepository);
    }
}