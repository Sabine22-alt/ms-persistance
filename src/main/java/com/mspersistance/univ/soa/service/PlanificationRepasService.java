package com.mspersistance.univ.soa.service;

import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.model.Activite;
import com.mspersistance.univ.soa.model.PlanificationRepas;
import com.mspersistance.univ.soa.model.PlanificationJour;
import com.mspersistance.univ.soa.model.RepasPlannifie;
import com.mspersistance.univ.soa.repository.PlanificationRepasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des planifications de repas.
 * Design Patterns: Constructor Injection + Cache-Aside
 */
@Service
public class PlanificationRepasService {

    private final PlanificationRepasRepository planificationRepasRepository;
    private final ActiviteService activiteService;

    public PlanificationRepasService(
            PlanificationRepasRepository planificationRepasRepository,
            ActiviteService activiteService) {
        this.planificationRepasRepository = planificationRepasRepository;
        this.activiteService = activiteService;
    }

    /**
     * Récupère ou crée la planification pour une semaine donnée
     */
    @Transactional
    public PlanificationRepas getOrCreatePlanification(Long utilisateurId, Integer semaine, Integer annee) {
        Optional<PlanificationRepas> existing = planificationRepasRepository
            .findByUtilisateurAndWeek(utilisateurId, semaine, annee);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Créer une nouvelle planification avec tous les jours de la semaine
        PlanificationRepas planification = new PlanificationRepas();
        planification.setUtilisateurId(utilisateurId);
        planification.setSemaine(semaine);
        planification.setAnnee(annee);

        // Initialiser les 7 jours (lundi à dimanche)
        for (int jour = 0; jour < 7; jour++) {
            PlanificationJour pj = new PlanificationJour();
            pj.setJour(jour);
            pj.setPlanification(planification);
            planification.getJours().add(pj);
        }

        return planificationRepasRepository.save(planification);
    }

    /**
     * Récupère la planification pour une semaine
     */
    @Transactional(readOnly = true)
    public Optional<PlanificationRepas> getPlanification(Long utilisateurId, Integer semaine, Integer annee) {
        return planificationRepasRepository.findByUtilisateurAndWeek(utilisateurId, semaine, annee);
    }

    /**
     * Ajouter/modifier un repas pour un jour
     */
    @Transactional
    public PlanificationRepas addOrUpdateRepas(Long utilisateurId, Integer semaine, Integer annee,
                                                Integer jour, Integer typeRepas, Long recetteId, String noteLibre) {
        PlanificationRepas planification = getOrCreatePlanification(utilisateurId, semaine, annee);

        PlanificationJour pj = planification.getJours().stream()
            .filter(j -> j.getJour().equals(jour))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Jour non trouvé"));

        // Vérifier si le repas existe déjà (modification) ou non (ajout)
        boolean isModification = pj.getRepas().stream()
            .anyMatch(r -> r.getTypeRepas().getValue() == typeRepas);

        // Chercher et supprimer si existe déjà ce type de repas
        pj.getRepas().removeIf(r -> r.getTypeRepas().getValue() == typeRepas);

        // Créer le nouveau repas
        RepasPlannifie repas = new RepasPlannifie();
        repas.setTypeRepas(RepasPlannifie.TypeRepas.values()[typeRepas]);
        repas.setRecetteId(recetteId);
        repas.setNoteLibre(noteLibre);
        repas.setPlanificationJour(pj);

        pj.getRepas().add(repas);

        PlanificationRepas saved = planificationRepasRepository.save(planification);

        // Enregistrer l'activité
        String[] joursNoms = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        String jourNom = joursNoms[jour];
        String typeRepasNom = RepasPlannifie.TypeRepas.values()[typeRepas].getLabel();
        String description = String.format("Repas planifié : %s - %s (Semaine %d/%d)",
            jourNom, typeRepasNom, semaine, annee);

        activiteService.logActivite(
            utilisateurId,
            isModification ? Activite.TypeActivite.REPAS_MODIFIE : Activite.TypeActivite.REPAS_PLANIFIE,
            description
        );

        return saved;
    }

    /**
     * Supprimer un repas planifié
     */
    @Transactional
    public PlanificationRepas deleteRepas(Long utilisateurId, Integer semaine, Integer annee,
                                          Integer jour, Integer typeRepas) {
        PlanificationRepas planification = planificationRepasRepository
            .findByUtilisateurAndWeek(utilisateurId, semaine, annee)
            .orElseThrow(() -> new ResourceNotFoundException("Planification non trouvée"));

        PlanificationJour pj = planification.getJours().stream()
            .filter(j -> j.getJour().equals(jour))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Jour non trouvé"));

        pj.getRepas().removeIf(r -> r.getTypeRepas().getValue() == typeRepas);

        PlanificationRepas saved = planificationRepasRepository.save(planification);

        // Enregistrer l'activité
        String[] joursNoms = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        String jourNom = joursNoms[jour];
        String typeRepasNom = RepasPlannifie.TypeRepas.values()[typeRepas].getLabel();
        String description = String.format("Repas supprimé : %s - %s (Semaine %d/%d)",
            jourNom, typeRepasNom, semaine, annee);

        activiteService.logActivite(
            utilisateurId,
            Activite.TypeActivite.REPAS_SUPPRIME,
            description
        );

        return saved;
    }

    /**
     * Récupère l'historique des planifications d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<PlanificationRepas> getPlanificationsHistory(Long utilisateurId) {
        return planificationRepasRepository.findByUtilisateurIdOrderByWeekDesc(utilisateurId);
    }
}

