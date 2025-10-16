// service/external/ExternalLookupService.java
package Hotel.jwt.service.external;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExternalLookupService {
    private final WebClient decolectaWebClient;

    public Map<String,Object> dni(String numero) {
        return decolectaWebClient.get()
                .uri(uri -> uri.path("/reniec/dni").queryParam("numero", numero).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class)
                        .flatMap(b -> Mono.error(new ResponseStatusException(r.statusCode(), b))))
                .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {})
                .block();
    }

    public Map<String,Object> ruc(String numero) {
        return decolectaWebClient.get()
                .uri(uri -> uri.path("/sunat/ruc").queryParam("numero", numero).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class)
                        .flatMap(b -> Mono.error(new ResponseStatusException(r.statusCode(), b))))
                .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {})
                .block();
    }
}
