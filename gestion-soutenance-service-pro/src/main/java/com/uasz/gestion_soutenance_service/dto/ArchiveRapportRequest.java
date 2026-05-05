package com.uasz.gestion_soutenance_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class ArchiveRapportRequest {

    @NotNull(message = "Le livrable rapport final est obligatoire")
    private Long livrableId;

    private String commentaireArchivage;
}
