package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.model.Activite;
import com.springbootTemplate.univ.soa.repository.ActiviteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActiviteService {

    @Autowired
    private ActiviteRepository activiteRepository;

    /**
     * Log une activité utilisateur
     */
    @Transactional
    public Activite logActivite(Long utilisateurId, Activite.TypeActivite type, String description) {
        return logActiviteWithDetails(utilisateurId, type, description, null);
    }

    /**
     * Log une activité avec détails (JSON)
     */
    @Transactional
    public Activite logActiviteWithDetails(Long utilisateurId, Activite.TypeActivite type, String description, String details) {
        Activite activite = new Activite();
        activite.setUtilisateurId(utilisateurId);
        activite.setType(type);
        activite.setDescription(description);
        activite.setDetails(details);

        return activiteRepository.save(activite);
    }

    /**
     * Récupère l'historique d'activité d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Activite> getActivitesByUtilisateur(Long utilisateurId) {
        return activiteRepository.findByUtilisateurIdOrderByDateActivityDesc(utilisateurId);
    }

    /**
     * Récupère les 10 dernières activités d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Activite> getLast10Activites(Long utilisateurId) {
        return activiteRepository.findLast10ActivitiesByUtilisateurId(utilisateurId);
    }
}

