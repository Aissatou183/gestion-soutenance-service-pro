package com.uasz.gestion_soutenance_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LivrableResponse {

    private Long id;
    private Long encadrementId;

    private String typeLivrable;
    private Integer version;
    private String statut;
    private Integer note;

    private String nomFichierOriginal;
    private String cheminFichier;
    private String typeFichier;
}