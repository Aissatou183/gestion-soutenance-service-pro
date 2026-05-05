package com.uasz.gestion_soutenance_service.dto;

import com.uasz.gestion_soutenance_service.entity.RoleJury;
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
    private RoleJury roleJury;
}
