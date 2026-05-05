package com.uasz.gestion_soutenance_service.repository;

import com.uasz.gestion_soutenance_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.*;

public interface SoutenanceRepository extends JpaRepository<Soutenance, Long> {
    List<Soutenance> findByEncadrementId(Long encadrementId);
    List<Soutenance> findByEtudiantId(Long etudiantId);
    List<Soutenance> findByEncadreurId(Long encadreurId);
    List<Soutenance> findByStatutOrderByDateHeureAsc(StatutSoutenance statut);
    List<Soutenance> findByDateHeureBetweenOrderByDateHeureAsc(LocalDateTime debut, LocalDateTime fin);
    boolean existsByEncadrementIdAndStatutIn(Long encadrementId, Collection<StatutSoutenance> statuts);
}
