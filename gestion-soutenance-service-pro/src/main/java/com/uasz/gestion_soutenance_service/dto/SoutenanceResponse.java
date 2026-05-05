package com.uasz.gestion_soutenance_service.dto;

import com.uasz.gestion_soutenance_service.entity.StatutSoutenance;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoutenanceResponse {
    private Long id;
    private Long encadrementId;
    private Long sujetId;
    private String sujetTitre;
    private Long etudiantId;
    private String etudiantNomComplet;
    private Long encadreurId;
    private String encadreurNomComplet;
    private LocalDateTime dateHeure;
    private String salle;
    private Integer dureeMinutes;
    private StatutSoutenance statut;
    private String observations;
    private LocalDateTime dateCreation;
    private List<MembreJuryResponse> jury;
}
