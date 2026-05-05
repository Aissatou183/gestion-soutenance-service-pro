package com.uasz.gestion_soutenance_service.client;

import com.uasz.gestion_soutenance_service.dto.EncadrementResponse;
import com.uasz.gestion_soutenance_service.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class EncadrementClient {

    private final RestTemplate restTemplate;

    @Value("${services.encadrement.url}")
    private String encadrementServiceUrl;

    public EncadrementResponse getEncadrement(Long id, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<EncadrementResponse> response = restTemplate.exchange(
                    encadrementServiceUrl + "/encadrements/" + id,
                    HttpMethod.GET,
                    entity,
                    EncadrementResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new BadRequestException("Impossible de récupérer l'encadrement : " + id);
        }
    }
}
