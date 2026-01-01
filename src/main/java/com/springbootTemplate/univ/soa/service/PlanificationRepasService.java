package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.exception.ResourceNotFoundException;
import com.springbootTemplate.univ.soa.model.PlanificationRepas;
import com.springbootTemplate.univ.soa.model.PlanificationJour;
import com.springbootTemplate.univ.soa.model.RepasPlannifie;
import com.springbootTemplate.univ.soa.repository.PlanificationRepasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PlanificationRepasService {

    @Autowired
    private PlanificationRepasRepository planificationRepasRepository;

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

        // Chercher et supprimer si existe déjà ce type de repas
        pj.getRepas().removeIf(r -> r.getTypeRepas().getValue() == typeRepas);

        // Créer le nouveau repas
        RepasPlannifie repas = new RepasPlannifie();
        repas.setTypeRepas(RepasPlannifie.TypeRepas.values()[typeRepas]);
        repas.setRecetteId(recetteId);
        repas.setNoteLibre(noteLibre);
        repas.setPlanificationJour(pj);

        pj.getRepas().add(repas);

        return planificationRepasRepository.save(planification);
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

        return planificationRepasRepository.save(planification);
    }

    /**
     * Récupère l'historique des planifications d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<PlanificationRepas> getPlanificationsHistory(Long utilisateurId) {
        return planificationRepasRepository.findByUtilisateurIdOrderByWeekDesc(utilisateurId);
    }
}

