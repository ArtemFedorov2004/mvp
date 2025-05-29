package io.github.artemfedorov2004.onlinestoreservice.service;

import io.github.artemfedorov2004.onlinestoreservice.entity.Customer;
import io.github.artemfedorov2004.onlinestoreservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DefaultCustomerService implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public void syncCustomerAndOidcUser(JwtAuthenticationToken token) {
        UUID oidcUserId = UUID.fromString(token.getName());
        String username = token.getToken()
                .getClaimAsString("preferred_username");

        this.customerRepository.findCustomerIdByOidcUserId(oidcUserId)
                .ifPresentOrElse((id) -> {
                    Customer customer = this.customerRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException(
                                    "Relation t_customer does not contain customer with id - %s".formatted(id.toString())));

                    customer.setUsername(username);
                }, () -> {
                    Customer customer = new Customer(null, username);

                    Customer saved = this.customerRepository.save(customer);
                    this.customerRepository.linkCustomerIdAndOidcUserId(saved.getId(), oidcUserId);
                });
    }

    @Override
    @Nullable
    public Customer getCurrentCustomer() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
            return null;
        }

        JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UUID oidcUserId = UUID.fromString(token.getName());

        UUID id = this.customerRepository.findCustomerIdByOidcUserId(oidcUserId)
                .orElseThrow(() -> new RuntimeException("Customer with given oidcUserId - %s not found"
                        .formatted(oidcUserId.toString())));

        return this.customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Relation t_customer does not contain customer with id - %s".formatted(id.toString())));
    }
}
