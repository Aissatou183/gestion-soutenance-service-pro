package com.uasz.gestion_soutenance_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveRapportResponse {
    private Long id;
    private Long soutenanceId;
    private Long livrableId;
    private Long encadrementId;
    private Long sujetId;
    private String sujetTitre;
    private Long etudiantId;
    private String etudiantNomComplet;
    private String nomFichierOriginal;
    private String cheminFichier;
    private String typeFichier;
    private LocalDateTime dateArchivage;
    private String commentaireArchivage;
}
