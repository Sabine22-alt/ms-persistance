package com.mspersistance.univ.soa.service;

import com.mspersistance.univ.soa.model.Activite;
import com.mspersistance.univ.soa.repository.ActiviteRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service pour la gestion des activitÃ©s utilisateur.
 * Design Patterns: Constructor Injection + Builder + Cache-Aside
 */
@Service
public class ActiviteService {

    private final ActiviteRepository activiteRepository;

    public ActiviteService(ActiviteRepository activiteRepository) {
        this.activiteRepository = activiteRepository;
    }

    /**
     * Log une activitÃ© utilisateur avec Builder Pattern
     */
    @Transactional
    public Activite logActivite(Long utilisateurId, Activite.TypeActivite type, String description) {
        return logActiviteWithDetails(utilisateurId, type, description, null);
    }

    /**
     * Log une activitÃ© avec dÃ©tails (JSON) - Utilisation du Builder
     */
    @Transactional
    public Activite logActiviteWithDetails(Long utilisateurId, Activite.TypeActivite type, String description, String details) {
        Activite activite = Activite.builder()
                .utilisateurId(utilisateurId)
                .type(type)
                .description(description)
                .details(details)
                .build();

        return activiteRepository.save(activite);
    }

    /**
     * RÃ©cupÃ¨re l'historique d'activitÃ© d'un utilisateur
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "activites", key = "'user:' + #utilisateurId")
    public List<Activite> getActivitesByUtilisateur(Long utilisateurId) {
        return activiteRepository.findByUtilisateurIdOrderByDateActivityDesc(utilisateurId);
    }

    /**
     * RÃ©cupÃ¨re les 10 derniÃ¨res activitÃ©s d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Activite> getLast10Activites(Long utilisateurId) {
        return activiteRepository.findLast10ActivitiesByUtilisateurId(utilisateurId);
    }
}

