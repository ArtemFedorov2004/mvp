package io.github.artemfedorov2004.onlinestoreservice.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Sql("/sql/customers.sql")
@Transactional
class CustomerRepositoryIT {

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void findCustomerIdByOidcUserId_CustomerExists_ReturnsCustomer() {
        // given
        UUID oidcUserId = UUID.fromString("b49730a2-bf0a-4dd0-94d8-07d5b1e257a8");

        // when
        Optional<UUID> id = this.customerRepository.findCustomerIdByOidcUserId(oidcUserId);

        // then
        assertTrue(id.isPresent());
        assertEquals(UUID.fromString("019c3b19-9a1f-45a7-b83c-5cb69289a309"), id.get());
    }

    @Test
    void findCustomerIdByOidcUserId_CustomerDoesNotExist_ReturnsEmptyOptional() {
        // given
        UUID oidcUserId = UUID.fromString("b49730a2-bf0a-4dd0-94d8-07d5b1e257a1");

        // when
        Optional<UUID> id = this.customerRepository.findCustomerIdByOidcUserId(oidcUserId);

        // then
        assertTrue(id.isEmpty());
    }
}