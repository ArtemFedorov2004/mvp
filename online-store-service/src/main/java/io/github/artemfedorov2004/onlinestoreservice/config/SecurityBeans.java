package io.github.artemfedorov2004.onlinestoreservice.config;

import io.github.artemfedorov2004.onlinestoreservice.security.CustomerOidcUserSynchronizerFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@RequiredArgsConstructor
@Configuration
public class SecurityBeans {

    private final CustomerOidcUserSynchronizerFilter customerOidcUserSynchronizerFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/online-store-api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/online-store-api/products/{productId:\\d+}/reviews")
                        .hasAuthority("SCOPE_create_product_review")
                        .requestMatchers(HttpMethod.PATCH, "/online-store-api/products/{productId:\\d+}/reviews")
                        .hasAuthority("SCOPE_create_product_review")
                        .requestMatchers(HttpMethod.DELETE, "/online-store-api/products/{productId:\\d+}/reviews")
                        .hasAuthority("SCOPE_create_product_review")
                        .requestMatchers("/actuator/**").hasAuthority("SCOPE_metrics")
                        .anyRequest().denyAll())
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .jwt(Customizer.withDefaults()))
                .oauth2Client(Customizer.withDefaults())
                .addFilterAfter(this.customerOidcUserSynchronizerFilter, AuthorizationFilter.class)
                .build();
    }
}
