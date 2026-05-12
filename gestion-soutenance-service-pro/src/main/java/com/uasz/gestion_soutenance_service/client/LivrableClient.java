package com.uasz.gestion_soutenance_service.client;

import com.uasz.gestion_soutenance_service.dto.LivrableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LivrableClient {

    private final RestTemplate restTemplate;

    @Value("${services.livrable.url}")
    private String livrableUrl;

    public List<LivrableResponse> livrablesParEncadrement(Long encadrementId, String token) {
        HttpHeaders headers = new HttpHeaders();

        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<LivrableResponse>> response =
                restTemplate.exchange(
                        livrableUrl + "/livrables/encadrement/" + encadrementId,
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<List<LivrableResponse>>() {}
                );

        return response.getBody() == null ? List.of() : response.getBody();
    }

    public LivrableResponse rapportFinalValide(Long encadrementId, String token) {
        return livrablesParEncadrement(encadrementId, token)
                .stream()
                .filter(l -> l.getTypeLivrable() != null)
                .filter(l -> l.getStatut() != null)
                .filter(l -> "RAPPORT_FINAL".equalsIgnoreCase(l.getTypeLivrable()))
                .filter(l ->
                        "VALIDE".equalsIgnoreCase(l.getStatut())
                                || "EVALUE".equalsIgnoreCase(l.getStatut())
                )
                .max(Comparator.comparing(l -> l.getVersion() == null ? 0 : l.getVersion()))
                .orElseThrow(() -> new RuntimeException(
                        "Impossible de planifier : aucun rapport final validé ou évalué."
                ));
    }
}