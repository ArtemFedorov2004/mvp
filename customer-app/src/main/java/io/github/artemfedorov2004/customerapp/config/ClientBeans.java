package io.github.artemfedorov2004.customerapp.config;

import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;
import de.codecentric.boot.admin.client.registration.RegistrationClient;
import io.github.artemfedorov2004.customerapp.client.DefaultProductsRestClient;
import io.github.artemfedorov2004.customerapp.client.DefaultReviewsRestClient;
import io.github.artemfedorov2004.customerapp.security.OAuthClientHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

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

    @Bean
    @ConditionalOnProperty(name = "spring.boot.admin.client.enabled", havingValue = "true")
    public RegistrationClient registrationClient(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                        authorizedClientService);

        RestTemplate restTemplate = new RestTemplateBuilder()
                .interceptors((request, body, execution) -> {
                    if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(OAuth2AuthorizeRequest
                                .withClientRegistrationId("metrics")
                                .principal("customer-app-metrics-client")
                                .build());

                        request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
                    }

                    return execution.execute(request, body);
                })
                .build();
        return new BlockingRegistrationClient(restTemplate);
    }
}
