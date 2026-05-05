package com.uasz.gestion_soutenance_service.controller;

import com.uasz.gestion_soutenance_service.dto.*;
import com.uasz.gestion_soutenance_service.service.ArchiveRapportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/archives")
@RequiredArgsConstructor
public class ArchiveRapportController {

    private final ArchiveRapportService service;

    private String token(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return "";
        }
        return authorizationHeader.substring(7);
    }

    @PostMapping("/soutenance/{soutenanceId}")
    public ArchiveRapportResponse archiver(
            @PathVariable Long soutenanceId,
            @Valid @RequestBody ArchiveRapportRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return service.archiver(soutenanceId, request, token(authorizationHeader));
    }

    @GetMapping
    public List<ArchiveRapportResponse> listerTous() {
        return service.listerTous();
    }

    @GetMapping("/soutenance/{soutenanceId}")
    public List<ArchiveRapportResponse> parSoutenance(@PathVariable Long soutenanceId) {
        return service.parSoutenance(soutenanceId);
    }

    @GetMapping("/etudiant/{etudiantId}")
    public List<ArchiveRapportResponse> parEtudiant(@PathVariable Long etudiantId) {
        return service.parEtudiant(etudiantId);
    }
}
