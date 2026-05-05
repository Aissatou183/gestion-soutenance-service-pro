package com.uasz.gestion_soutenance_service.service;

import com.uasz.gestion_soutenance_service.client.LivrableClient;
import com.uasz.gestion_soutenance_service.dto.*;
import com.uasz.gestion_soutenance_service.entity.*;
import com.uasz.gestion_soutenance_service.exception.BadRequestException;
import com.uasz.gestion_soutenance_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArchiveRapportService {

    private final ArchiveRapportRepository archiveRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final LivrableClient livrableClient;

    public ArchiveRapportResponse archiver(Long soutenanceId, ArchiveRapportRequest request, String token) {
        Soutenance soutenance = soutenanceRepository.findById(soutenanceId)
                .orElseThrow(() -> new BadRequestException("Soutenance introuvable"));

        if (soutenance.getStatut() != StatutSoutenance.TERMINEE) {
            throw new BadRequestException("Le rapport final ne peut être archivé qu'après soutenance terminée");
        }

        if (archiveRepository.existsByLivrableId(request.getLivrableId())) {
            throw new BadRequestException("Ce rapport final est déjà archivé");
        }

        LivrableResponse livrable = livrableClient.getLivrable(request.getLivrableId(), token);

        if (livrable == null) {
            throw new BadRequestException("Livrable introuvable");
        }

        if (!"RAPPORT_FINAL".equals(livrable.getTypeLivrable())) {
            throw new BadRequestException("Seul un rapport final peut être archivé");
        }

        if (!soutenance.getEncadrementId().equals(livrable.getEncadrementId())) {
            throw new BadRequestException("Le rapport final ne correspond pas à cette soutenance");
        }

        ArchiveRapport archive = ArchiveRapport.builder()
                .soutenanceId(soutenanceId)
                .livrableId(livrable.getId())
                .encadrementId(livrable.getEncadrementId())
                .sujetId(livrable.getSujetId())
                .sujetTitre(livrable.getSujetTitre())
                .etudiantId(livrable.getEtudiantId())
                .etudiantNomComplet(livrable.getEtudiantNomComplet())
                .nomFichierOriginal(livrable.getNomFichierOriginal())
                .cheminFichier(livrable.getCheminFichier())
                .typeFichier(livrable.getTypeFichier())
                .dateArchivage(LocalDateTime.now())
                .commentaireArchivage(request.getCommentaireArchivage())
                .build();

        return toResponse(archiveRepository.save(archive));
    }

    public List<ArchiveRapportResponse> listerTous() {
        return archiveRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<ArchiveRapportResponse> parSoutenance(Long soutenanceId) {
        return archiveRepository.findBySoutenanceId(soutenanceId).stream().map(this::toResponse).toList();
    }

    public List<ArchiveRapportResponse> parEtudiant(Long etudiantId) {
        return archiveRepository.findByEtudiantId(etudiantId).stream().map(this::toResponse).toList();
    }

    private ArchiveRapportResponse toResponse(ArchiveRapport a) {
        return ArchiveRapportResponse.builder()
                .id(a.getId())
                .soutenanceId(a.getSoutenanceId())
                .livrableId(a.getLivrableId())
                .encadrementId(a.getEncadrementId())
                .sujetId(a.getSujetId())
                .sujetTitre(a.getSujetTitre())
                .etudiantId(a.getEtudiantId())
                .etudiantNomComplet(a.getEtudiantNomComplet())
                .nomFichierOriginal(a.getNomFichierOriginal())
                .cheminFichier(a.getCheminFichier())
                .typeFichier(a.getTypeFichier())
                .dateArchivage(a.getDateArchivage())
                .commentaireArchivage(a.getCommentaireArchivage())
                .build();
    }
}
