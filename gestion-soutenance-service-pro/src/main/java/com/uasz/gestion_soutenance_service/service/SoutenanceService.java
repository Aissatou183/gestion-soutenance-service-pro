package com.uasz.gestion_soutenance_service.service;

import com.uasz.gestion_soutenance_service.client.EncadrementClient;
import com.uasz.gestion_soutenance_service.client.LivrableClient;
import com.uasz.gestion_soutenance_service.client.UtilisateurClient;
import com.uasz.gestion_soutenance_service.dto.*;
import com.uasz.gestion_soutenance_service.entity.*;
import com.uasz.gestion_soutenance_service.repository.ArchiveRapportRepository;
import com.uasz.gestion_soutenance_service.repository.MembreJuryRepository;
import com.uasz.gestion_soutenance_service.repository.SoutenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SoutenanceService {

    private final SoutenanceRepository soutenanceRepository;
    private final MembreJuryRepository membreJuryRepository;
    private final ArchiveRapportRepository archiveRapportRepository;

    private final EncadrementClient encadrementClient;
    private final LivrableClient livrableClient;
    private final UtilisateurClient utilisateurClient;

    @Transactional
    public SoutenanceResponse planifier(SoutenanceRequest request, String token, String role) {

        verifierAdmin(role);
        verifierRequest(request);

        if (request.getDateSoutenance().isBefore(LocalDate.now())) {
            throw new RuntimeException("La date de soutenance ne peut pas être passée.");
        }

        if (soutenanceRepository.existsByEncadrementId(request.getEncadrementId())) {
            throw new RuntimeException("Une soutenance existe déjà pour cet encadrement.");
        }

        EncadrementResponse encadrement =
                encadrementClient.trouverParId(request.getEncadrementId(), token);

        if (encadrement == null) {
            throw new RuntimeException("Encadrement introuvable.");
        }

        if (!"ACTIF".equalsIgnoreCase(String.valueOf(encadrement.getStatut()))) {
            throw new RuntimeException("Impossible de planifier : l'encadrement n'est pas actif.");
        }

        LivrableResponse rapportFinal =
                livrableClient.rapportFinalValide(request.getEncadrementId(), token);

        verifierConflitSalle(
                request.getDateSoutenance(),
                request.getSalle(),
                request.getHeureDebut(),
                request.getHeureFin(),
                null
        );

        Soutenance soutenance = Soutenance.builder()
                .encadrementId(encadrement.getId())
                .sujetId(encadrement.getSujetId())
                .sujetTitre(encadrement.getSujetTitre())
                .etudiantId(encadrement.getEtudiantId())
                .etudiantNomComplet(encadrement.getEtudiantNomComplet())
                .dateSoutenance(request.getDateSoutenance())
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .salle(request.getSalle().trim())
                .statut(StatutSoutenance.PLANIFIEE)
                .rapportFinalId(rapportFinal.getId())
                .rapportArchive(false)
                .build();

        soutenance = soutenanceRepository.save(soutenance);

        ajouterEncadreurAutomatique(soutenance, encadrement, token);
        ajouterMembresChoisis(soutenance, request.getMembres(), token);

        verifierJuryComplet(soutenance.getId());

        return map(soutenance);
    }

    public List<SoutenanceResponse> listerTous(String role) {
        verifierAdmin(role);

        return soutenanceRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    public List<SoutenanceResponse> mesSoutenances(Long userId, String role) {

        if ("ETUDIANT".equals(role)) {
            return soutenanceRepository.findByEtudiantId(userId)
                    .stream()
                    .map(this::map)
                    .toList();
        }

        if ("ENSEIGNANT".equals(role)) {
            return membreJuryRepository.findByEnseignantId(userId)
                    .stream()
                    .map(MembreJury::getSoutenanceId)
                    .distinct()
                    .map(id -> soutenanceRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Soutenance introuvable.")))
                    .map(this::map)
                    .toList();
        }

        if ("ADMINISTRATEUR".equals(role)) {
            return soutenanceRepository.findAll()
                    .stream()
                    .map(this::map)
                    .toList();
        }

        throw new RuntimeException("Rôle non autorisé.");
    }

    public SoutenanceResponse trouver(Long id) {
        return map(get(id));
    }

    public SoutenanceResponse trouverSelonRole(Long id, Long userId, String role) {
        SoutenanceResponse response = trouver(id);

        if ("ADMINISTRATEUR".equals(role)) {
            return response;
        }

        if ("ETUDIANT".equals(role)
                && response.getEtudiantId() != null
                && response.getEtudiantId().equals(userId)) {
            return response;
        }

        if ("ENSEIGNANT".equals(role)
                && response.getJury() != null
                && response.getJury().stream()
                .anyMatch(j -> j.getEnseignantId() != null
                        && j.getEnseignantId().equals(userId))) {
            return response;
        }

        throw new RuntimeException("Accès refusé à cette soutenance.");
    }

    @Transactional
    public SoutenanceResponse modifier(Long id, SoutenanceRequest request, String token, String role) {

        verifierAdmin(role);
        verifierRequest(request);

        Soutenance s = get(id);

        if (s.getStatut() == StatutSoutenance.ARCHIVEE) {
            throw new RuntimeException("Impossible de modifier une soutenance archivée.");
        }

        if (s.getStatut() == StatutSoutenance.TERMINEE) {
            throw new RuntimeException("Impossible de modifier une soutenance terminée.");
        }

        if (s.getStatut() == StatutSoutenance.ANNULEE) {
            throw new RuntimeException("Impossible de modifier une soutenance annulée.");
        }

        verifierConflitSalle(
                request.getDateSoutenance(),
                request.getSalle(),
                request.getHeureDebut(),
                request.getHeureFin(),
                id
        );

        s.setDateSoutenance(request.getDateSoutenance());
        s.setHeureDebut(request.getHeureDebut());
        s.setHeureFin(request.getHeureFin());
        s.setSalle(request.getSalle().trim());

        soutenanceRepository.save(s);

        membreJuryRepository.deleteBySoutenanceId(id);

        EncadrementResponse encadrement =
                encadrementClient.trouverParId(s.getEncadrementId(), token);

        if (encadrement == null) {
            throw new RuntimeException("Encadrement introuvable.");
        }

        ajouterEncadreurAutomatique(s, encadrement, token);
        ajouterMembresChoisis(s, request.getMembres(), token);

        verifierJuryComplet(id);

        return map(s);
    }

    public SoutenanceResponse terminer(Long id, String role) {

        verifierAdmin(role);

        Soutenance s = get(id);

        if (s.getStatut() == StatutSoutenance.ANNULEE) {
            throw new RuntimeException("Impossible de terminer une soutenance annulée.");
        }

        if (s.getStatut() == StatutSoutenance.ARCHIVEE) {
            throw new RuntimeException("Cette soutenance est déjà archivée.");
        }

        s.setStatut(StatutSoutenance.TERMINEE);

        return map(soutenanceRepository.save(s));
    }

    public SoutenanceResponse annuler(Long id, String role) {

        verifierAdmin(role);

        Soutenance s = get(id);

        if (s.getStatut() == StatutSoutenance.ARCHIVEE) {
            throw new RuntimeException("Impossible d'annuler une soutenance archivée.");
        }

        if (s.getStatut() == StatutSoutenance.TERMINEE) {
            throw new RuntimeException("Impossible d'annuler une soutenance terminée.");
        }

        s.setStatut(StatutSoutenance.ANNULEE);

        return map(soutenanceRepository.save(s));
    }

    @Transactional
    public SoutenanceResponse archiver(Long id, String role) {

        verifierAdmin(role);

        Soutenance s = get(id);

        if (s.getStatut() != StatutSoutenance.TERMINEE) {
            throw new RuntimeException("La soutenance doit être terminée avant archivage.");
        }

        if (Boolean.TRUE.equals(s.getRapportArchive())) {
            throw new RuntimeException("Le rapport final est déjà archivé.");
        }

        ArchiveRapport archive = ArchiveRapport.builder()
                .soutenanceId(s.getId())
                .livrableId(s.getRapportFinalId())
                .encadrementId(s.getEncadrementId())
                .sujetId(s.getSujetId())
                .sujetTitre(s.getSujetTitre())
                .etudiantId(s.getEtudiantId())
                .etudiantNomComplet(s.getEtudiantNomComplet())
                .nomFichierOriginal("rapport-final-" + s.getRapportFinalId())
                .cheminFichier("archive/rapport-final-" + s.getRapportFinalId())
                .typeFichier("PDF")
                .dateArchivage(LocalDateTime.now())
                .commentaireArchivage("Archivage automatique du rapport final après soutenance.")
                .build();

        archiveRapportRepository.save(archive);

        s.setRapportArchive(true);
        s.setStatut(StatutSoutenance.ARCHIVEE);
        s.setDateArchivage(LocalDateTime.now());

        return map(soutenanceRepository.save(s));
    }

    @Transactional
    public void supprimer(Long id, String role) {

        verifierAdmin(role);

        Soutenance s = get(id);

        if (s.getStatut() == StatutSoutenance.ARCHIVEE) {
            throw new RuntimeException("Impossible de supprimer une soutenance archivée.");
        }

        membreJuryRepository.deleteBySoutenanceId(id);
        soutenanceRepository.delete(s);
    }

    private void ajouterEncadreurAutomatique(Soutenance s, EncadrementResponse encadrement, String token) {

        if (encadrement.getEnseignantId() == null) {
            throw new RuntimeException("Encadreur principal introuvable.");
        }

        UtilisateurResponse u =
                utilisateurClient.trouverParId(encadrement.getEnseignantId(), token);

        if (u == null) {
            throw new RuntimeException("Encadreur introuvable.");
        }

        if (!"ENSEIGNANT".equalsIgnoreCase(String.valueOf(u.getRole()))) {
            throw new RuntimeException("L'encadreur doit être un enseignant.");
        }

        verifierDisponibiliteEnseignant(
                encadrement.getEnseignantId(),
                s.getDateSoutenance(),
                s.getHeureDebut(),
                s.getHeureFin(),
                s.getId()
        );

        MembreJury membre = MembreJury.builder()
                .soutenanceId(s.getId())
                .enseignantId(encadrement.getEnseignantId())
                .enseignantNomComplet(encadrement.getEnseignantNomComplet())
                .enseignantEmail(u.getEmail())
                .roleJury(RoleJury.ENCADREUR)
                .build();

        membreJuryRepository.save(membre);
    }

    private void ajouterMembresChoisis(Soutenance s, List<MembreJuryRequest> membres, String token) {

        if (membres == null || membres.isEmpty()) {
            throw new RuntimeException("Le jury doit contenir au moins un président et un examinateur.");
        }

        long presidents = membres.stream()
                .filter(m -> m.getRoleJury() == RoleJury.PRESIDENT)
                .count();

        if (presidents != 1) {
            throw new RuntimeException("Le jury doit contenir un seul président.");
        }

        long examinateurs = membres.stream()
                .filter(m -> m.getRoleJury() == RoleJury.EXAMINATEUR)
                .count();

        if (examinateurs < 1) {
            throw new RuntimeException("Le jury doit contenir au moins un examinateur.");
        }

        for (MembreJuryRequest m : membres) {

            if (m.getEnseignantId() == null) {
                throw new RuntimeException("L'enseignant du jury est obligatoire.");
            }

            if (m.getRoleJury() == null) {
                throw new RuntimeException("Le rôle du membre du jury est obligatoire.");
            }

            if (m.getRoleJury() == RoleJury.ENCADREUR) {
                throw new RuntimeException("L'encadreur est ajouté automatiquement.");
            }

            if (membreJuryRepository.existsBySoutenanceIdAndEnseignantId(
                    s.getId(),
                    m.getEnseignantId()
            )) {
                throw new RuntimeException("Un enseignant ne peut pas être ajouté deux fois au même jury.");
            }

            verifierDisponibiliteEnseignant(
                    m.getEnseignantId(),
                    s.getDateSoutenance(),
                    s.getHeureDebut(),
                    s.getHeureFin(),
                    s.getId()
            );

            UtilisateurResponse u =
                    utilisateurClient.trouverParId(m.getEnseignantId(), token);

            if (u == null) {
                throw new RuntimeException("Enseignant introuvable : " + m.getEnseignantId());
            }

            if (!"ENSEIGNANT".equalsIgnoreCase(String.valueOf(u.getRole()))) {
                throw new RuntimeException("Le membre du jury doit être un enseignant.");
            }

            MembreJury membre = MembreJury.builder()
                    .soutenanceId(s.getId())
                    .enseignantId(u.getId())
                    .enseignantNomComplet(utilisateurClient.nomComplet(u))
                    .enseignantEmail(u.getEmail())
                    .roleJury(m.getRoleJury())
                    .build();

            membreJuryRepository.save(membre);
        }
    }

    private void verifierJuryComplet(Long soutenanceId) {

        boolean president = membreJuryRepository.existsBySoutenanceIdAndRoleJury(
                soutenanceId,
                RoleJury.PRESIDENT
        );

        boolean encadreur = membreJuryRepository.existsBySoutenanceIdAndRoleJury(
                soutenanceId,
                RoleJury.ENCADREUR
        );

        boolean examinateur = membreJuryRepository.existsBySoutenanceIdAndRoleJury(
                soutenanceId,
                RoleJury.EXAMINATEUR
        );

        if (!president) {
            throw new RuntimeException("Le jury doit contenir un président.");
        }

        if (!encadreur) {
            throw new RuntimeException("Le jury doit contenir l'encadreur.");
        }

        if (!examinateur) {
            throw new RuntimeException("Le jury doit contenir au moins un examinateur.");
        }
    }

    private void verifierDisponibiliteEnseignant(
            Long enseignantId,
            LocalDate date,
            LocalTime debut,
            LocalTime fin,
            Long soutenanceCouranteId
    ) {
        boolean conflit = soutenanceRepository.findByDateSoutenance(date)
                .stream()
                .filter(s ->
                        s.getStatut() != StatutSoutenance.ANNULEE
                                && s.getStatut() != StatutSoutenance.ARCHIVEE
                )
                .filter(s -> soutenanceCouranteId == null || !s.getId().equals(soutenanceCouranteId))
                .filter(s ->
                        membreJuryRepository.findBySoutenanceId(s.getId())
                                .stream()
                                .anyMatch(m -> m.getEnseignantId().equals(enseignantId))
                )
                .anyMatch(s -> chevauche(debut, fin, s.getHeureDebut(), s.getHeureFin()));

        if (conflit) {
            throw new RuntimeException("Un membre du jury est déjà occupé sur ce créneau.");
        }
    }

    private void verifierConflitSalle(
            LocalDate date,
            String salle,
            LocalTime debut,
            LocalTime fin,
            Long soutenanceCouranteId
    ) {
        boolean conflit = soutenanceRepository.findByDateSoutenanceAndSalle(date, salle.trim())
                .stream()
                .filter(s ->
                        s.getStatut() != StatutSoutenance.ANNULEE
                                && s.getStatut() != StatutSoutenance.ARCHIVEE
                )
                .filter(s -> soutenanceCouranteId == null || !s.getId().equals(soutenanceCouranteId))
                .anyMatch(s -> chevauche(debut, fin, s.getHeureDebut(), s.getHeureFin()));

        if (conflit) {
            throw new RuntimeException("La salle est déjà occupée sur ce créneau.");
        }
    }

    private boolean chevauche(LocalTime d1, LocalTime f1, LocalTime d2, LocalTime f2) {
        return d1.isBefore(f2) && f1.isAfter(d2);
    }

    private void verifierRequest(SoutenanceRequest request) {

        if (request == null) {
            throw new RuntimeException("La requête est obligatoire.");
        }

        if (request.getEncadrementId() == null) {
            throw new RuntimeException("L'encadrement est obligatoire.");
        }

        if (request.getDateSoutenance() == null) {
            throw new RuntimeException("La date de soutenance est obligatoire.");
        }

        if (request.getSalle() == null || request.getSalle().trim().isEmpty()) {
            throw new RuntimeException("La salle est obligatoire.");
        }

        verifierHeures(request.getHeureDebut(), request.getHeureFin());
    }

    private void verifierHeures(LocalTime debut, LocalTime fin) {

        if (debut == null || fin == null) {
            throw new RuntimeException("Heure début et heure fin obligatoires.");
        }

        if (!fin.isAfter(debut)) {
            throw new RuntimeException("L'heure de fin doit être après l'heure de début.");
        }
    }

    private void verifierAdmin(String role) {
        if (!"ADMINISTRATEUR".equals(role)) {
            throw new RuntimeException("Seul l'administrateur peut effectuer cette action.");
        }
    }

    private Soutenance get(Long id) {
        return soutenanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Soutenance introuvable."));
    }

    private SoutenanceResponse map(Soutenance s) {

        List<MembreJuryResponse> jury =
                membreJuryRepository.findBySoutenanceId(s.getId())
                        .stream()
                        .map(m -> MembreJuryResponse.builder()
                                .id(m.getId())
                                .soutenanceId(m.getSoutenanceId())
                                .enseignantId(m.getEnseignantId())
                                .enseignantNomComplet(m.getEnseignantNomComplet())
                                .enseignantEmail(m.getEnseignantEmail())
                                .roleJury(m.getRoleJury().name())
                                .build())
                        .toList();

        return SoutenanceResponse.builder()
                .id(s.getId())
                .encadrementId(s.getEncadrementId())
                .sujetId(s.getSujetId())
                .sujetTitre(s.getSujetTitre())
                .etudiantId(s.getEtudiantId())
                .etudiantNomComplet(s.getEtudiantNomComplet())
                .dateSoutenance(s.getDateSoutenance())
                .heureDebut(s.getHeureDebut())
                .heureFin(s.getHeureFin())
                .salle(s.getSalle())
                .statut(s.getStatut().name())
                .rapportFinalId(s.getRapportFinalId())
                .rapportArchive(s.getRapportArchive())
                .dateCreation(s.getDateCreation())
                .dateArchivage(s.getDateArchivage())
                .jury(jury)
                .build();
    }
}