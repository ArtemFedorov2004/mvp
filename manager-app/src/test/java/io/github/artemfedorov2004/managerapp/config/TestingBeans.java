package io.github.artemfedorov2004.managerapp.config;

import io.github.artemfedorov2004.managerapp.client.DefaultProductsRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

import static org.mockito.Mockito.mock;

@Configuration
public class TestingBeans {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return mock(ClientRegistrationRepository.class);
    }

    @Bean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository() {
        return mock(OAuth2AuthorizedClientRepository.class);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return mock();
    }

    @Bean
    @Primary
    public DefaultProductsRestClient testProductsRestClient(
            @Value("${online-store.services.online-store-api.uri:http://localhost:54321}")
            String onlineStoreServiceBaseUri
    ) {
        var client = (HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build());
        var requestFactory = new JdkClientHttpRequestFactory(client);
        return new DefaultProductsRestClient(RestClient.builder()
                .baseUrl(onlineStoreServiceBaseUri)
                .requestFactory(requestFactory)
                .build());
    }
}
