package io.github.artemfedorov2004.customerapp.config;

import io.github.artemfedorov2004.customerapp.client.DefaultProductsRestClient;
import io.github.artemfedorov2004.customerapp.client.DefaultReviewsRestClient;
import io.github.artemfedorov2004.customerapp.security.OAuthClientHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientBeans {

    @Bean
    @Scope("prototype")
    public RestClient.Builder onlineStoreServicesRestClientBuilder(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository,
            @Value("${online-store.services.online-store-service.registration-id:keycloak}")
            String registrationId
    ) {
        OAuthClientHttpRequestInterceptor oAuthClientHttpRequestInterceptor =
                new OAuthClientHttpRequestInterceptor(
                        new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository,
                                authorizedClientRepository), registrationId);

        return RestClient.builder()
                .requestInterceptor(oAuthClientHttpRequestInterceptor);
    }

    @Bean
    public DefaultProductsRestClient productsRestClient(
            @Value("${online-store.services.online-store-service.uri:http://localhost:8080}")
            String onlineStoreServiceBaseUri,
            RestClient.Builder onlineStoreServicesRestClientBuilder) {
        return new DefaultProductsRestClient(onlineStoreServicesRestClientBuilder
                .baseUrl(onlineStoreServiceBaseUri)
                .build());
    }

    @Bean
    public DefaultReviewsRestClient reviewsRestClient(
            @Value("${online-store.services.online-store-service.uri:http://localhost:8080}")
            String onlineStoreServiceBaseUri,
            RestClient.Builder onlineStoreServicesRestClientBuilder
    ) {
        return new DefaultReviewsRestClient(onlineStoreServicesRestClientBuilder
                .baseUrl(onlineStoreServiceBaseUri)
                .build());
    }
}
