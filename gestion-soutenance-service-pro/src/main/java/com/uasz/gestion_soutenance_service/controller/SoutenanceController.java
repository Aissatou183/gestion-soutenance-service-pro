package com.uasz.gestion_soutenance_service.controller;

import com.uasz.gestion_soutenance_service.dto.*;
import com.uasz.gestion_soutenance_service.service.SoutenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/soutenances")
@RequiredArgsConstructor
public class SoutenanceController {

    private final SoutenanceService service;

    private String token(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return "";
        }
        return authorizationHeader.substring(7);
    }

    @PostMapping
    public SoutenanceResponse planifier(
            @Valid @RequestBody PlanificationSoutenanceRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return service.planifier(request, token(authorizationHeader));
    }

    @PostMapping("/{id}/jury")
    public MembreJuryResponse ajouterMembreJury(
            @PathVariable Long id,
            @Valid @RequestBody MembreJuryRequest request
    ) {
        return service.ajouterMembreJury(id, request);
    }

    @PutMapping("/{id}/reporter")
    public SoutenanceResponse reporter(
            @PathVariable Long id,
            @Valid @RequestBody PlanificationSoutenanceRequest request
    ) {
        return service.reporter(id, request);
    }

    @PutMapping("/{id}/terminer")
    public SoutenanceResponse terminer(@PathVariable Long id) {
        return service.terminer(id);
    }

    @PutMapping("/{id}/annuler")
    public SoutenanceResponse annuler(@PathVariable Long id) {
        return service.annuler(id);
    }

    @GetMapping
    public List<SoutenanceResponse> listerTous() {
        return service.listerTous();
    }

    @GetMapping("/planning")
    public List<SoutenanceResponse> planning() {
        return service.planning();
    }

    @GetMapping("/mes-soutenances")
    public List<SoutenanceResponse> mesSoutenances(@RequestHeader("Authorization") String authorizationHeader) {
        return service.mesSoutenances(token(authorizationHeader));
    }

    @GetMapping("/periode")
    public List<SoutenanceResponse> entreDeuxDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin
    ) {
        return service.entreDeuxDates(debut, fin);
    }

    @GetMapping("/{id}")
    public SoutenanceResponse trouverParId(@PathVariable Long id) {
        return service.trouverParId(id);
    }

    @DeleteMapping("/jury/{membreId}")
    public Map<String, String> supprimerMembreJury(@PathVariable Long membreId) {
        service.supprimerMembreJury(membreId);
        return Map.of("message", "Membre du jury supprimé avec succès");
    }

    @DeleteMapping("/{id}")
    public Map<String, String> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return Map.of("message", "Soutenance supprimée avec succès");
    }
}
