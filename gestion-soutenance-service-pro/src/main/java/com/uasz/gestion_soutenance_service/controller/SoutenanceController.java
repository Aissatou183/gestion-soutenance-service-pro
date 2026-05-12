package com.uasz.gestion_soutenance_service.controller;

import com.uasz.gestion_soutenance_service.dto.SoutenanceRequest;
import com.uasz.gestion_soutenance_service.dto.SoutenanceResponse;
import com.uasz.gestion_soutenance_service.security.JwtService;
import com.uasz.gestion_soutenance_service.service.SoutenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/soutenances")
@RequiredArgsConstructor
public class SoutenanceController {

    private final SoutenanceService soutenanceService;
    private final JwtService jwtService;

    private String token(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token JWT manquant ou invalide."
            );
        }

        return authorizationHeader.substring(7);
    }

    private String role(String token) {
        return jwtService.extractRole(token);
    }

    private Long userId(String token) {
        return jwtService.extractUserId(token);
    }

    @PostMapping
    public SoutenanceResponse planifier(
            @Valid @RequestBody SoutenanceRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = token(authorizationHeader);
        return soutenanceService.planifier(request, token, role(token));
    }

    @GetMapping
    public List<SoutenanceResponse> listerTous(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = token(authorizationHeader);
        return soutenanceService.listerTous(role(token));
    }

    @GetMapping("/mes-soutenances")
    public List<SoutenanceResponse> mesSoutenances(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = token(authorizationHeader);
        return soutenanceService.mesSoutenances(userId(token), role(token));
    }

    @GetMapping("/{id}")
    public SoutenanceResponse trouver(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = token(authorizationHeader);
        return soutenanceService.trouverSelonRole(id, userId(token), role(token));
    }

    @PutMapping("/{id}")
    public SoutenanceResponse modifier(
            @PathVariable Long id,
            @Valid @RequestBody SoutenanceRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = token(authorizationHeader);
        return soutenanceService.modifier(id, request, token, role(token));
    }

    @PutMapping("/{id}/terminer")
    public SoutenanceResponse terminer(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = token(authorizationHeader);
        return soutenanceService.terminer(id, role(token));
    }

    @PutMapping("/{id}/annuler")
    public SoutenanceResponse annuler(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = token(authorizationHeader);
        return soutenanceService.annuler(id, role(token));
    }

    @PutMapping("/{id}/archiver")
    public SoutenanceResponse archiver(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = token(authorizationHeader);
        return soutenanceService.archiver(id, role(token));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> supprimer(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = token(authorizationHeader);
        soutenanceService.supprimer(id, role(token));

        return Map.of("message", "Soutenance supprimée avec succès.");
    }
}