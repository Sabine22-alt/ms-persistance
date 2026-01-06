package com.mspersistance.univ.soa.mapper;

import com.mspersistance.univ.soa.dto.FeedbackDTO;
import com.mspersistance.univ.soa.model.Feedback;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Feedback et FeedbackDTO (Record).
 */
@Component
public class FeedbackMapper {

    /**
     * Convertit une entité Feedback en DTO Record
     */
    public FeedbackDTO toDTO(Feedback feedback) {
        if (feedback == null) {
            return null;
        }

        return new FeedbackDTO(
                feedback.getId(),
                feedback.getUtilisateur() != null ? feedback.getUtilisateur().getId() : null,
                feedback.getRecette() != null ? feedback.getRecette().getId() : null,
                feedback.getEvaluation(),
                feedback.getCommentaire(),
                feedback.getDateFeedback(),
                feedback.getDateModification()
        );
    }

    /**
     * Convertit un DTO Record en entité Feedback (partiel)
     * Note: Les relations (utilisateur/recette) doivent être résolues séparément
     */
    public Feedback toEntity(FeedbackDTO dto) {
        if (dto == null) {
            return null;
        }

        Feedback feedback = new Feedback();
        feedback.setId(dto.id());
        feedback.setEvaluation(dto.evaluation());
        feedback.setCommentaire(dto.commentaire());
        feedback.setDateFeedback(dto.dateFeedback());
        feedback.setDateModification(dto.dateModification());
        return feedback;
    }
}