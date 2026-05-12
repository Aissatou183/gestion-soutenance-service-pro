package com.uasz.gestion_soutenance_service.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private LocalDate dateSoutenance;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    private String salle;
    private String statut;

    private Long rapportFinalId;
    private Boolean rapportArchive;

    private LocalDateTime dateCreation;
    private LocalDateTime dateArchivage;

    private List<MembreJuryResponse> jury;
}