package com.uasz.gestion_soutenance_service.client;

import com.uasz.gestion_soutenance_service.dto.LivrableResponse;
import com.uasz.gestion_soutenance_service.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class LivrableClient {

    private final RestTemplate restTemplate;

    @Value("${services.livrable.url}")
    private String livrableServiceUrl;

    public LivrableResponse getLivrable(Long id, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<LivrableResponse> response = restTemplate.exchange(
                    livrableServiceUrl + "/livrables/" + id,
                    HttpMethod.GET,
                    entity,
                    LivrableResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new BadRequestException("Impossible de récupérer le livrable : " + id);
        }
    }
}
