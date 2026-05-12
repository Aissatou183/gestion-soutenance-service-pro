package com.uasz.gestion_soutenance_service.dto;

import com.uasz.gestion_soutenance_service.entity.RoleJury;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembreJuryRequest {

    @NotNull(message = "L'enseignant est obligatoire.")
    private Long enseignantId;

    @NotNull(message = "Le rôle dans le jury est obligatoire.")
    private RoleJury roleJury;
}