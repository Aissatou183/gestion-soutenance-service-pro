package com.uasz.gestion_soutenance_service.repository;

import com.uasz.gestion_soutenance_service.entity.Soutenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SoutenanceRepository extends JpaRepository<Soutenance, Long> {

    boolean existsByEncadrementId(Long encadrementId);

    List<Soutenance> findByEtudiantId(Long etudiantId);

    List<Soutenance> findByDateSoutenance(LocalDate dateSoutenance);

    List<Soutenance> findByDateSoutenanceAndSalle(LocalDate dateSoutenance, String salle);
}