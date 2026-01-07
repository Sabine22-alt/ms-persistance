package com.mspersistance.univ.soa.factory;

import com.mspersistance.univ.soa.dto.FeedbackDTO;
import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.model.Feedback;
import com.mspersistance.univ.soa.model.Recette;
import com.mspersistance.univ.soa.model.Utilisateur;
import com.mspersistance.univ.soa.repository.RecetteRepository;
import com.mspersistance.univ.soa.repository.UtilisateurRepository;
import org.springframework.stereotype.Component;

/**
 * Factory pour crÃ©er des entitÃ©s Feedback.
 * Design Pattern: Factory Method + Builder
 */
@Component
public class FeedbackFactory {

    private final UtilisateurRepository utilisateurRepository;
    private final RecetteRepository recetteRepository;

    public FeedbackFactory(UtilisateurRepository utilisateurRepository, RecetteRepository recetteRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.recetteRepository = recetteRepository;
    }

    /**
     * CrÃ©e un Feedback depuis un DTO Record
     */
    public Feedback createFromDTO(FeedbackDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findById(dto.utilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvÃ© avec l'ID: " + dto.utilisateurId()));

        Recette recette = recetteRepository.findByIdSimple(dto.recetteId())
                .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvÃ©e avec l'ID: " + dto.recetteId()));

        return Feedback.builder()
                .evaluation(dto.evaluation())
                .commentaire(dto.commentaire())
                .utilisateur(utilisateur)
                .recette(recette)
                .build();
    }

    /**
     * Met Ã  jour un Feedback existant
     */
    public Feedback updateFromDTO(Feedback existing, FeedbackDTO dto) {
        existing.setEvaluation(dto.evaluation());
        existing.setCommentaire(dto.commentaire());
        return existing;
    }

    /**
     * Convertit une entitÃ© en DTO Record
     */
    public FeedbackDTO toDTO(Feedback feedback) {
        return new FeedbackDTO(
                feedback.getId(),
                feedback.getUtilisateur().getId(),
                feedback.getRecette().getId(),
                feedback.getEvaluation(),
                feedback.getCommentaire(),
                feedback.getDateFeedback(),
                feedback.getDateModification()
        );
    }
}

