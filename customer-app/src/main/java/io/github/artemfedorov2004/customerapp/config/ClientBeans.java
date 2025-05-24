package io.github.artemfedorov2004.customerapp.config;

import io.github.artemfedorov2004.customerapp.client.DefaultProductsRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientBeans {

    @Bean
    public DefaultProductsRestClient productsRestClient(
            @Value("${online-store.services.online-store-service.uri:http://localhost:8080}")
            String onlineStoreServiceBaseUri) {
        return new DefaultProductsRestClient(RestClient.builder()
                .baseUrl(onlineStoreServiceBaseUri)
                .build());
    }
}
