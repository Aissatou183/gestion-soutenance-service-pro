package com.uasz.gestion_soutenance_service.dto;

import com.uasz.gestion_soutenance_service.entity.RoleJury;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class MembreJuryRequest {

    @NotNull(message = "L'identifiant de l'enseignant est obligatoire")
    private Long enseignantId;

    @NotBlank(message = "Le nom complet est obligatoire")
    private String enseignantNomComplet;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String enseignantEmail;

    @NotNull(message = "Le rôle dans le jury est obligatoire")
    private RoleJury roleJury;
}
