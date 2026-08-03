package org.scraper.webscraper.restClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /** These environment variables need to be injected via a secrets.env file. */
    @Bean
    public RestClient restClient(
            @Value("${BASE_URL}") String baseUrl,
            @Value("${ADMIN_USERNAME}") String username,
            @Value("${ADMIN_PASSWORD}") String password) {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor(new BasicAuthenticationInterceptor(username, password))
                .build();
    }
}