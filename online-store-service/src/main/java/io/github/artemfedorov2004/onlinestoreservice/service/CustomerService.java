package io.github.artemfedorov2004.onlinestoreservice.service;

import io.github.artemfedorov2004.onlinestoreservice.entity.Customer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public interface CustomerService {

    void syncCustomerAndOidcUser(JwtAuthenticationToken token);

    Customer getCurrentCustomer();
}
