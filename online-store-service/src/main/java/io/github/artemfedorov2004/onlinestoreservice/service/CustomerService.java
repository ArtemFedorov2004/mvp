package io.github.artemfedorov2004.onlinestoreservice.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public interface CustomerService {

    void syncCustomerAndOidcUser(JwtAuthenticationToken token);
}
