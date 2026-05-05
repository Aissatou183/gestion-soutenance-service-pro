package com.uasz.gestion_soutenance_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LivrableResponse {
    private Long id;
    private Long encadrementId;
    private Long sujetId;
    private String sujetTitre;
    private Long etudiantId;
    private String etudiantNomComplet;
    private Long enseignantId;
    private String enseignantNomComplet;
    private String typeLivrable;
    private Integer version;
    private String nomFichierOriginal;
    private String cheminFichier;
    private String typeFichier;
    private String statut;
}
