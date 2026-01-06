package com.mspersistance.univ.soa.dto;

import java.time.LocalDateTime;

/**
 * DTO Feedback en Record - Immutable et performant
 */
public record FeedbackDTO(
    Long id,
    Long utilisateurId,
    Long recetteId,
    Integer evaluation,
    String commentaire,
    LocalDateTime dateFeedback,
    LocalDateTime dateModification
) {
    // Validation handled at controller/service level to allow returning proper HTTP errors
}