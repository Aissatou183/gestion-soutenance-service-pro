package com.uasz.gestion_soutenance_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembreJuryResponse {

    private Long id;
    private Long soutenanceId;

    private Long enseignantId;
    private String enseignantNomComplet;
    private String enseignantEmail;

    private String roleJury;
}