package com.mspersistance.univ.soa.mapper;

import com.mspersistance.univ.soa.dto.FeedbackDTO;
import com.mspersistance.univ.soa.model.Feedback;
import com.mspersistance.univ.soa.model.Recette;
import com.mspersistance.univ.soa.model.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires pour FeedbackMapper")
class FeedbackMapperTest {

    private FeedbackMapper feedbackMapper;

    @BeforeEach
    void setUp() {
        feedbackMapper = new FeedbackMapper();
    }

    // ==================== Tests pour toDTO() ====================

    @Test
    @DisplayName("toDTO - avec entitÃ© complÃ¨te, devrait convertir en DTO")
    void toDTO_avecEntiteComplete_devraitConvertirEnDTO() {
        // Given
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);

        Recette recette = new Recette();
        recette.setId(2L);

        LocalDateTime now = LocalDateTime.now();

        Feedback feedback = new Feedback();
        feedback.setId(10L);
        feedback.setEvaluation(5);
        feedback.setCommentaire("Excellent!");
        feedback.setDateFeedback(now);
        feedback.setDateModification(now);
        feedback.setUtilisateur(utilisateur);
        feedback.setRecette(recette);

        // When
        FeedbackDTO dto = feedbackMapper.toDTO(feedback);

        // Then
        assertNotNull(dto);
        assertEquals(10L, dto.id());
        assertEquals(5, dto.evaluation());
        assertEquals("Excellent!", dto.commentaire());
        assertEquals(now, dto.dateFeedback());
        assertEquals(now, dto.dateModification());
        assertEquals(1L, dto.utilisateurId());
        assertEquals(2L, dto.recetteId());
    }

    @Test
    @DisplayName("toDTO - avec entitÃ© null, devrait retourner null")
    void toDTO_avecEntiteNull_devraitRetournerNull() {
        // When
        FeedbackDTO dto = feedbackMapper.toDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("toDTO - sans utilisateur, utilisateurId devrait Ãªtre null")
    void toDTO_sansUtilisateur_utilisateurIdDevraitEtreNull() {
        // Given
        Feedback feedback = new Feedback();
        feedback.setId(10L);
        feedback.setEvaluation(4);

        // When
        FeedbackDTO dto = feedbackMapper.toDTO(feedback);

        // Then
        assertNotNull(dto);
        assertNull(dto.utilisateurId());
    }

    @Test
    @DisplayName("toDTO - sans recette, recetteId devrait Ãªtre null")
    void toDTO_sansRecette_recetteIdDevraitEtreNull() {
        // Given
        Feedback feedback = new Feedback();
        feedback.setId(10L);
        feedback.setEvaluation(4);

        // When
        FeedbackDTO dto = feedbackMapper.toDTO(feedback);

        // Then
        assertNotNull(dto);
        assertNull(dto.recetteId());
    }

    // ==================== Tests pour toEntity() ====================

    @Test
    @DisplayName("toEntity - avec DTO valide, devrait convertir en entitÃ©")
    void toEntity_avecDTOValide_devraitConvertirEnEntite() {
        LocalDateTime now = LocalDateTime.now();

        FeedbackDTO dto = new FeedbackDTO(10L, 1L, 2L, 3, "Bon", now, now);

        Feedback feedback = feedbackMapper.toEntity(dto);

        assertNotNull(feedback);
        assertEquals(10L, feedback.getId());
        assertEquals(3, feedback.getEvaluation());
        assertEquals("Bon", feedback.getCommentaire());
        assertEquals(now, feedback.getDateFeedback());
        assertEquals(now, feedback.getDateModification());
        assertNull(feedback.getUtilisateur());
        assertNull(feedback.getRecette());
    }

    @Test
    @DisplayName("toEntity - avec DTO null, devrait retourner null")
    void toEntity_avecDTONull_devraitRetournerNull() {
        // When
        Feedback feedback = feedbackMapper.toEntity(null);

        // Then
        assertNull(feedback);
    }

    @Test
    @DisplayName("toEntity - avec commentaire null, devrait fonctionner")
    void toEntity_avecCommentaireNull_devraitFonctionner() {
        FeedbackDTO dto = new FeedbackDTO(10L, null, null, 5, null, null, null);

        Feedback feedback = feedbackMapper.toEntity(dto);

        assertNotNull(feedback);
        assertNull(feedback.getCommentaire());
    }

    // ==================== Tests de conversion bidirectionnelle ====================

    @Test
    @DisplayName("conversion bidirectionnelle - devrait prÃ©server les donnÃ©es principales")
    void conversionBidirectionnelle_devraitPreserverLesDonneesPrincipales() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Feedback original = new Feedback();
        original.setId(15L);
        original.setEvaluation(4);
        original.setCommentaire("TrÃ¨s bon");
        original.setDateFeedback(now);
        original.setDateModification(now);

        // When
        FeedbackDTO dto = feedbackMapper.toDTO(original);
        Feedback converti = feedbackMapper.toEntity(dto);

        // Then
        assertNotNull(converti);
        assertEquals(original.getId(), converti.getId());
        assertEquals(original.getEvaluation(), converti.getEvaluation());
        assertEquals(original.getCommentaire(), converti.getCommentaire());
        assertEquals(original.getDateFeedback(), converti.getDateFeedback());
        assertEquals(original.getDateModification(), converti.getDateModification());
    }
}
