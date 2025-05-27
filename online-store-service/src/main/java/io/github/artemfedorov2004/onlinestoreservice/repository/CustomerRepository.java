package io.github.artemfedorov2004.onlinestoreservice.repository;

import io.github.artemfedorov2004.onlinestoreservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            insert into online_store.t_customer_oidcuser
            values (:id, :oidcUserId)
            """, nativeQuery = true)
    void linkCustomerIdAndOidcUserId(UUID id, UUID oidcUserId);

    @Query(value = """
            select id_customer from online_store.t_customer_oidcuser 
                               where id_oidcuser=:oidcUserId;
            """, nativeQuery = true)
    Optional<UUID> findCustomerIdByOidcUserId(UUID oidcUserId);
}
