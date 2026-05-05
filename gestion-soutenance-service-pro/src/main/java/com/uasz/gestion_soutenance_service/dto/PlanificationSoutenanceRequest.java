package com.uasz.gestion_soutenance_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class PlanificationSoutenanceRequest {

    @NotNull(message = "L'encadrement est obligatoire")
    private Long encadrementId;

    @NotNull(message = "La date et l'heure sont obligatoires")
    private LocalDateTime dateHeure;

    @NotBlank(message = "La salle est obligatoire")
    private String salle;

    @NotNull(message = "La durée est obligatoire")
    @Min(value = 15, message = "La durée minimale est 15 minutes")
    private Integer dureeMinutes;

    private String observations;
}
