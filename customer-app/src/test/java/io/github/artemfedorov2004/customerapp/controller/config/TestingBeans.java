package io.github.artemfedorov2004.customerapp.controller.config;

import io.github.artemfedorov2004.customerapp.client.DefaultProductsRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TestingBeans {

    @Bean
    @Primary
    public DefaultProductsRestClient testProductsRestClient(
            @Value("${online-store.services.online-store-api.uri:http://localhost:54321}")
            String onlineStoreServiceBaseUri
    ) {
        return new DefaultProductsRestClient(RestClient.builder()
                .baseUrl(onlineStoreServiceBaseUri)
                .requestFactory(new JdkClientHttpRequestFactory())
                .build());
    }
}
