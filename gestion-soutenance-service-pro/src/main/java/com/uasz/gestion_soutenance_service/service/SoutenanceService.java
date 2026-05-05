package com.uasz.gestion_soutenance_service.service;

import com.uasz.gestion_soutenance_service.client.*;
import com.uasz.gestion_soutenance_service.dto.*;
import com.uasz.gestion_soutenance_service.entity.*;
import com.uasz.gestion_soutenance_service.exception.BadRequestException;
import com.uasz.gestion_soutenance_service.repository.*;
import com.uasz.gestion_soutenance_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SoutenanceService {

    private final SoutenanceRepository soutenanceRepository;
    private final MembreJuryRepository membreJuryRepository;
    private final EncadrementClient encadrementClient;
    private final JwtService jwtService;

    public SoutenanceResponse planifier(PlanificationSoutenanceRequest request, String token) {
        EncadrementResponse encadrement = encadrementClient.getEncadrement(request.getEncadrementId(), token);

        if (encadrement == null) {
            throw new BadRequestException("Encadrement introuvable");
        }

        if (!"ACTIF".equals(encadrement.getStatut()) && !"TERMINE".equals(encadrement.getStatut())) {
            throw new BadRequestException("L'encadrement doit être actif ou terminé pour planifier une soutenance");
        }

        boolean existe = soutenanceRepository.existsByEncadrementIdAndStatutIn(
                request.getEncadrementId(),
                List.of(StatutSoutenance.PLANIFIEE, StatutSoutenance.REPORTEE)
        );

        if (existe) {
            throw new BadRequestException("Une soutenance est déjà planifiée pour cet encadrement");
        }

        Soutenance soutenance = Soutenance.builder()
                .encadrementId(encadrement.getId())
                .sujetId(encadrement.getSujetId())
                .sujetTitre(encadrement.getSujetTitre())
                .etudiantId(encadrement.getEtudiantId())
                .etudiantNomComplet(encadrement.getEtudiantNomComplet())
                .encadreurId(encadrement.getEnseignantId())
                .encadreurNomComplet(encadrement.getEnseignantNomComplet())
                .dateHeure(request.getDateHeure())
                .salle(request.getSalle())
                .dureeMinutes(request.getDureeMinutes())
                .statut(StatutSoutenance.PLANIFIEE)
                .observations(request.getObservations())
                .dateCreation(LocalDateTime.now())
                .build();

        Soutenance saved = soutenanceRepository.save(soutenance);

        MembreJury encadreur = MembreJury.builder()
                .soutenanceId(saved.getId())
                .enseignantId(encadrement.getEnseignantId())
                .enseignantNomComplet(encadrement.getEnseignantNomComplet())
                .enseignantEmail("non-renseigne@univ-zig.sn")
                .roleJury(RoleJury.ENCADREUR)
                .build();

        membreJuryRepository.save(encadreur);

        return toResponse(saved);
    }

    public MembreJuryResponse ajouterMembreJury(Long soutenanceId, MembreJuryRequest request) {
        Soutenance soutenance = soutenanceRepository.findById(soutenanceId)
                .orElseThrow(() -> new BadRequestException("Soutenance introuvable"));

        if (soutenance.getStatut() == StatutSoutenance.TERMINEE || soutenance.getStatut() == StatutSoutenance.ANNULEE) {
            throw new BadRequestException("Impossible de modifier le jury d'une soutenance terminée ou annulée");
        }

        if (membreJuryRepository.existsBySoutenanceIdAndEnseignantId(soutenanceId, request.getEnseignantId())) {
            throw new BadRequestException("Cet enseignant est déjà membre du jury");
        }

        MembreJury membre = MembreJury.builder()
                .soutenanceId(soutenanceId)
                .enseignantId(request.getEnseignantId())
                .enseignantNomComplet(request.getEnseignantNomComplet())
                .enseignantEmail(request.getEnseignantEmail())
                .roleJury(request.getRoleJury())
                .build();

        return toJuryResponse(membreJuryRepository.save(membre));
    }

    public SoutenanceResponse reporter(Long id, PlanificationSoutenanceRequest request) {
        Soutenance soutenance = soutenanceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Soutenance introuvable"));

        soutenance.setDateHeure(request.getDateHeure());
        soutenance.setSalle(request.getSalle());
        soutenance.setDureeMinutes(request.getDureeMinutes());
        soutenance.setObservations(request.getObservations());
        soutenance.setStatut(StatutSoutenance.REPORTEE);

        return toResponse(soutenanceRepository.save(soutenance));
    }

    public SoutenanceResponse terminer(Long id) {
        Soutenance soutenance = soutenanceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Soutenance introuvable"));

        soutenance.setStatut(StatutSoutenance.TERMINEE);
        return toResponse(soutenanceRepository.save(soutenance));
    }

    public SoutenanceResponse annuler(Long id) {
        Soutenance soutenance = soutenanceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Soutenance introuvable"));

        soutenance.setStatut(StatutSoutenance.ANNULEE);
        return toResponse(soutenanceRepository.save(soutenance));
    }

    public List<SoutenanceResponse> listerTous() {
        return soutenanceRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<SoutenanceResponse> planning() {
        return soutenanceRepository.findByStatutOrderByDateHeureAsc(StatutSoutenance.PLANIFIEE)
                .stream().map(this::toResponse).toList();
    }

    public List<SoutenanceResponse> entreDeuxDates(LocalDateTime debut, LocalDateTime fin) {
        return soutenanceRepository.findByDateHeureBetweenOrderByDateHeureAsc(debut, fin)
                .stream().map(this::toResponse).toList();
    }

    public List<SoutenanceResponse> mesSoutenances(String token) {
        Long userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);

        if ("ETUDIANT".equals(role)) {
            return soutenanceRepository.findByEtudiantId(userId).stream().map(this::toResponse).toList();
        }

        if ("ENSEIGNANT".equals(role)) {
            Set<Long> ids = new LinkedHashSet<>();
            soutenanceRepository.findByEncadreurId(userId).forEach(s -> ids.add(s.getId()));
            membreJuryRepository.findByEnseignantId(userId).forEach(j -> ids.add(j.getSoutenanceId()));

            return ids.stream()
                    .map(id -> soutenanceRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .map(this::toResponse)
                    .toList();
        }

        return listerTous();
    }

    public SoutenanceResponse trouverParId(Long id) {
        return soutenanceRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new BadRequestException("Soutenance introuvable"));
    }

    public void supprimerMembreJury(Long membreId) {
        if (!membreJuryRepository.existsById(membreId)) {
            throw new BadRequestException("Membre de jury introuvable");
        }
        membreJuryRepository.deleteById(membreId);
    }

    public void supprimer(Long id) {
        if (!soutenanceRepository.existsById(id)) {
            throw new BadRequestException("Soutenance introuvable");
        }
        soutenanceRepository.deleteById(id);
    }

    private SoutenanceResponse toResponse(Soutenance s) {
        List<MembreJuryResponse> jury = membreJuryRepository.findBySoutenanceId(s.getId())
                .stream().map(this::toJuryResponse).toList();

        return SoutenanceResponse.builder()
                .id(s.getId())
                .encadrementId(s.getEncadrementId())
                .sujetId(s.getSujetId())
                .sujetTitre(s.getSujetTitre())
                .etudiantId(s.getEtudiantId())
                .etudiantNomComplet(s.getEtudiantNomComplet())
                .encadreurId(s.getEncadreurId())
                .encadreurNomComplet(s.getEncadreurNomComplet())
                .dateHeure(s.getDateHeure())
                .salle(s.getSalle())
                .dureeMinutes(s.getDureeMinutes())
                .statut(s.getStatut())
                .observations(s.getObservations())
                .dateCreation(s.getDateCreation())
                .jury(jury)
                .build();
    }

    private MembreJuryResponse toJuryResponse(MembreJury m) {
        return MembreJuryResponse.builder()
                .id(m.getId())
                .soutenanceId(m.getSoutenanceId())
                .enseignantId(m.getEnseignantId())
                .enseignantNomComplet(m.getEnseignantNomComplet())
                .enseignantEmail(m.getEnseignantEmail())
                .roleJury(m.getRoleJury())
                .build();
    }
}
