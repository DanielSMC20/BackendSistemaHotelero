package Hotel.jwt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpClientsConfig {

    @Bean
    public WebClient decolectaWebClient(
            @Value("${decolecta.base}") String base,
            @Value("${decolecta.token}") String token) {

        return WebClient.builder()
                .baseUrl(base)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }
}