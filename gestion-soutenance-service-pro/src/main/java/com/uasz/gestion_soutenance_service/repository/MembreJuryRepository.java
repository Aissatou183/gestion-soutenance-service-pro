package com.uasz.gestion_soutenance_service.repository;

import com.uasz.gestion_soutenance_service.entity.MembreJury;
import com.uasz.gestion_soutenance_service.entity.RoleJury;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembreJuryRepository extends JpaRepository<MembreJury, Long> {

    List<MembreJury> findBySoutenanceId(Long soutenanceId);

    List<MembreJury> findByEnseignantId(Long enseignantId);

    boolean existsBySoutenanceIdAndEnseignantId(Long soutenanceId, Long enseignantId);

    boolean existsBySoutenanceIdAndRoleJury(Long soutenanceId, RoleJury roleJury);

    void deleteBySoutenanceId(Long soutenanceId);
}