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
public class Soutenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private Long encadreurId;

    @Column(nullable=false)
    private String encadreurNomComplet;

    @Column(nullable=false)
    private LocalDateTime dateHeure;

    @Column(nullable=false)
    private String salle;

    @Column(nullable=false)
    private Integer dureeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private StatutSoutenance statut;

    @Column(columnDefinition = "TEXT")
    private String observations;

    private LocalDateTime dateCreation;
}
