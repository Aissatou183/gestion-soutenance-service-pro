package com.uasz.gestion_soutenance_service.client;

import com.uasz.gestion_soutenance_service.dto.UtilisateurResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UtilisateurClient {

    private final RestTemplate restTemplate;

    @Value("${services.utilisateur.url}")
    private String utilisateurUrl;

    public UtilisateurResponse trouverParId(Long id, String token) {
        HttpHeaders headers = new HttpHeaders();

        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UtilisateurResponse> response =
                restTemplate.exchange(
                        utilisateurUrl + "/utilisateurs/" + id,
                        HttpMethod.GET,
                        entity,
                        UtilisateurResponse.class
                );

        return response.getBody();
    }

    public String nomComplet(UtilisateurResponse u) {
        if (u == null) return "";

        String prenom = u.getPrenom() == null ? "" : u.getPrenom();
        String nom = u.getNom() == null ? "" : u.getNom();

        return (prenom + " " + nom).trim();
    }
}