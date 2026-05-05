package com.uasz.gestion_soutenance_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"soutenanceId", "enseignantId"})
})
public class MembreJury {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long soutenanceId;

    @Column(nullable=false)
    private Long enseignantId;

    @Column(nullable=false)
    private String enseignantNomComplet;

    @Column(nullable=false)
    private String enseignantEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private RoleJury roleJury;
}
