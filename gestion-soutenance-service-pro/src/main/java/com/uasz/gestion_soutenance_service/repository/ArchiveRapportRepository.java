package com.uasz.gestion_soutenance_service.repository;

import com.uasz.gestion_soutenance_service.entity.ArchiveRapport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchiveRapportRepository extends JpaRepository<ArchiveRapport, Long> {
    List<ArchiveRapport> findBySoutenanceId(Long soutenanceId);
    List<ArchiveRapport> findByEtudiantId(Long etudiantId);
    boolean existsByLivrableId(Long livrableId);
}
