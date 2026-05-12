package com.uasz.gestion_soutenance_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        indexes = {
                @Index(name = "idx_soutenance_encadrement", columnList = "encadrementId"),
                @Index(name = "idx_soutenance_date_salle", columnList = "dateSoutenance,salle")
        }
)
public class Soutenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long encadrementId;

    private Long sujetId;

    @Column(length = 255)
    private String sujetTitre;

    private Long etudiantId;

    @Column(length = 180)
    private String etudiantNomComplet;

    @Column(nullable = false)
    private LocalDate dateSoutenance;

    @Column(nullable = false)
    private LocalTime heureDebut;

    @Column(nullable = false)
    private LocalTime heureFin;

    @Column(nullable = false, length = 120)
    private String salle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutSoutenance statut;

    private Long rapportFinalId;

    @Column(nullable = false)
    private Boolean rapportArchive;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateArchivage;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (statut == null) statut = StatutSoutenance.PLANIFIEE;
        if (rapportArchive == null) rapportArchive = false;
    }
}