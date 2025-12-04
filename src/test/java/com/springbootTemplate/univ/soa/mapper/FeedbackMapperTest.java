package com.springbootTemplate.univ.soa.mapper;

import com.springbootTemplate.univ.soa.dto.FeedbackDTO;
import com.springbootTemplate.univ.soa.model.Feedback;
import com.springbootTemplate.univ.soa.model.Recette;
import com.springbootTemplate.univ.soa.model.Utilisateur;
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
    @DisplayName("toDTO - avec entité complète, devrait convertir en DTO")
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
        assertEquals(10L, dto.getId());
        assertEquals(5, dto.getEvaluation());
        assertEquals("Excellent!", dto.getCommentaire());
        assertEquals(now, dto.getDateFeedback());
        assertEquals(now, dto.getDateModification());
        assertEquals(1L, dto.getUtilisateurId());
        assertEquals(2L, dto.getRecetteId());
    }

    @Test
    @DisplayName("toDTO - avec entité null, devrait retourner null")
    void toDTO_avecEntiteNull_devraitRetournerNull() {
        // When
        FeedbackDTO dto = feedbackMapper.toDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("toDTO - sans utilisateur, utilisateurId devrait être null")
    void toDTO_sansUtilisateur_utilisateurIdDevraitEtreNull() {
        // Given
        Feedback feedback = new Feedback();
        feedback.setId(10L);
        feedback.setEvaluation(4);

        // When
        FeedbackDTO dto = feedbackMapper.toDTO(feedback);

        // Then
        assertNotNull(dto);
        assertNull(dto.getUtilisateurId());
    }

    @Test
    @DisplayName("toDTO - sans recette, recetteId devrait être null")
    void toDTO_sansRecette_recetteIdDevraitEtreNull() {
        // Given
        Feedback feedback = new Feedback();
        feedback.setId(10L);
        feedback.setEvaluation(4);

        // When
        FeedbackDTO dto = feedbackMapper.toDTO(feedback);

        // Then
        assertNotNull(dto);
        assertNull(dto.getRecetteId());
    }

    // ==================== Tests pour toEntity() ====================

    @Test
    @DisplayName("toEntity - avec DTO valide, devrait convertir en entité")
    void toEntity_avecDTOValide_devraitConvertirEnEntite() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(10L);
        dto.setEvaluation(3);
        dto.setCommentaire("Bon");
        dto.setDateFeedback(now);
        dto.setDateModification(now);
        dto.setUtilisateurId(1L);
        dto.setRecetteId(2L);

        // When
        Feedback feedback = feedbackMapper.toEntity(dto);

        // Then
        assertNotNull(feedback);
        assertEquals(10L, feedback.getId());
        assertEquals(3, feedback.getEvaluation());
        assertEquals("Bon", feedback.getCommentaire());
        assertEquals(now, feedback.getDateFeedback());
        assertEquals(now, feedback.getDateModification());
        // Note: utilisateur et recette ne sont pas mappés dans toEntity
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
        // Given
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(10L);
        dto.setEvaluation(5);
        dto.setCommentaire(null);

        // When
        Feedback feedback = feedbackMapper.toEntity(dto);

        // Then
        assertNotNull(feedback);
        assertNull(feedback.getCommentaire());
    }

    // ==================== Tests de conversion bidirectionnelle ====================

    @Test
    @DisplayName("conversion bidirectionnelle - devrait préserver les données principales")
    void conversionBidirectionnelle_devraitPreserverLesDonneesPrincipales() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Feedback original = new Feedback();
        original.setId(15L);
        original.setEvaluation(4);
        original.setCommentaire("Très bon");
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