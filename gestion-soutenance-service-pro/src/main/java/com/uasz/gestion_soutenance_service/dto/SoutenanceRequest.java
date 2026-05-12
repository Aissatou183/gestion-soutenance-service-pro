package com.uasz.gestion_soutenance_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoutenanceRequest {

    @NotNull(message = "L'encadrement est obligatoire.")
    private Long encadrementId;

    @NotNull(message = "La date est obligatoire.")
    private LocalDate dateSoutenance;

    @NotNull(message = "L'heure de début est obligatoire.")
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire.")
    private LocalTime heureFin;

    @NotBlank(message = "La salle est obligatoire.")
    private String salle;

    @Valid
    @NotEmpty(message = "Le jury est obligatoire.")
    private List<MembreJuryRequest> membres;
}