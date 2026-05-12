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
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_soutenance_enseignant",
                        columnNames = {"soutenanceId", "enseignantId"}
                )
        },
        indexes = {
                @Index(name = "idx_membre_soutenance", columnList = "soutenanceId"),
                @Index(name = "idx_membre_enseignant", columnList = "enseignantId")
        }
)
public class MembreJury {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long soutenanceId;

    @Column(nullable = false)
    private Long enseignantId;

    @Column(nullable = false, length = 150)
    private String enseignantNomComplet;

    @Column(nullable = false, length = 180)
    private String enseignantEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoleJury roleJury;

    @Column(nullable = false)
    private LocalDateTime dateAjout;

    @PrePersist
    public void prePersist() {
        if (dateAjout == null) dateAjout = LocalDateTime.now();
    }
}