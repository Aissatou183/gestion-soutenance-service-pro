package com.uasz.gestion_soutenance_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ArchiveRapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long soutenanceId;

    @Column(nullable=false)
    private Long livrableId;

    @Column(nullable=false)
    private Long encadrementId;

    @Column(nullable=false)
    private Long sujetId;

    @Column(nullable=false)
    private String sujetTitre;

    @Column(nullable=false)
    private Long etudiantId;

    @Column(nullable=false)
    private String etudiantNomComplet;

    @Column(nullable=false)
    private String nomFichierOriginal;

    @Column(nullable=false)
    private String cheminFichier;

    @Column(nullable=false)
    private String typeFichier;

    @Column(nullable = false)
    private LocalDateTime dateArchivage;

    @Column(columnDefinition = "TEXT")
    private String commentaireArchivage;

    @PrePersist
    public void prePersist() {
        if (dateArchivage == null) {
            dateArchivage = LocalDateTime.now();
        }
    }
}