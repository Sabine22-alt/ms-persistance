package com.springbootTemplate.univ.soa.mapper;

import com.springbootTemplate.univ.soa.dto.FeedbackDTO;
import com.springbootTemplate.univ.soa.model.Feedback;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public FeedbackDTO toDTO(Feedback feedback) {
        if (feedback == null) {
            return null;
        }

        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(feedback.getId());
        dto.setEvaluation(feedback.getEvaluation());
        dto.setCommentaire(feedback.getCommentaire());
        dto.setDateFeedback(feedback.getDateFeedback());
        dto.setDateModification(feedback.getDateModification());

        if (feedback.getUtilisateur() != null) {
            dto.setUtilisateurId(feedback.getUtilisateur().getId());
        }

        if (feedback.getRecette() != null) {
            dto.setRecetteId(feedback.getRecette().getId());
        }

        return dto;
    }

    public Feedback toEntity(FeedbackDTO dto) {
        if (dto == null) {
            return null;
        }

        Feedback feedback = new Feedback();
        feedback.setId(dto.getId());
        feedback.setEvaluation(dto.getEvaluation());
        feedback.setCommentaire(dto.getCommentaire());
        feedback.setDateFeedback(dto.getDateFeedback());
        feedback.setDateModification(dto.getDateModification());
        return feedback;
    }
}