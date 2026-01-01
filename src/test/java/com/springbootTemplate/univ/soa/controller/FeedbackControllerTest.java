package com.springbootTemplate.univ.soa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbootTemplate.univ.soa.config.TestSecurityConfig;
import com.springbootTemplate.univ.soa.dto.FeedbackDTO;
import com.springbootTemplate.univ.soa.mapper.FeedbackMapper;
import com.springbootTemplate.univ.soa.model.Feedback;
import com.springbootTemplate.univ.soa.model.Recette;
import com.springbootTemplate.univ.soa.model.Utilisateur;
import com.springbootTemplate.univ.soa.service.FeedbackService;
import com.springbootTemplate.univ.soa.service.RecetteService;
import com.springbootTemplate.univ.soa.service.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(FeedbackController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("Tests unitaires pour FeedbackController")
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FeedbackService feedbackService;

    @MockitoBean
    private UtilisateurService utilisateurService;

    @MockitoBean
    private RecetteService recetteService;

    @MockitoBean
    private FeedbackMapper feedbackMapper;

    private Feedback feedback;
    private FeedbackDTO feedbackDTO;
    private Utilisateur utilisateur;
    private Recette recette;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1L);

        recette = new Recette();
        recette.setId(1L);

        feedback = new Feedback();
        feedback.setId(1L);
        feedback.setEvaluation(5);
        feedback.setCommentaire("Excellent!");
        feedback.setUtilisateur(utilisateur);
        feedback.setRecette(recette);

        feedbackDTO = new FeedbackDTO();
        feedbackDTO.setId(1L);
        feedbackDTO.setEvaluation(5);
        feedbackDTO.setCommentaire("Excellent!");
        feedbackDTO.setUtilisateurId(1L);
        feedbackDTO.setRecetteId(1L);
    }

    // ==================== Tests pour GET /api/persistance/feedbacks ====================

    @Test
    @DisplayName("GET /api/persistance/feedbacks - devrait retourner tous les feedbacks")
    void getAllFeedbacks_devraitRetournerTousLesFeedbacks() throws Exception {
        // Given
        when(feedbackService.findAll()).thenReturn(Arrays.asList(feedback));
        when(feedbackMapper.toDTO(feedback)).thenReturn(feedbackDTO);

        // When & Then
        mockMvc.perform(get("/api/persistance/feedbacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].evaluation").value(5));

        verify(feedbackService, times(1)).findAll();
    }

    // ==================== Tests pour GET /api/persistance/feedbacks/{id} ====================

    @Test
    @DisplayName("GET /api/persistance/feedbacks/{id} - avec ID existant, devrait retourner le feedback")
    void getFeedbackById_avecIdExistant_devraitRetournerFeedback() throws Exception {
        // Given
        when(feedbackService.findById(1L)).thenReturn(Optional.of(feedback));
        when(feedbackMapper.toDTO(feedback)).thenReturn(feedbackDTO);

        // When & Then
        mockMvc.perform(get("/api/persistance/feedbacks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.evaluation").value(5))
                .andExpect(jsonPath("$.commentaire").value("Excellent!"));

        verify(feedbackService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /api/persistance/feedbacks/{id} - avec ID inexistant, devrait retourner 404")
    void getFeedbackById_avecIdInexistant_devraitRetourner404() throws Exception {
        // Given
        when(feedbackService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/persistance/feedbacks/999"))
                .andExpect(status().isNotFound());

        verify(feedbackService, times(1)).findById(999L);
    }

    // ==================== Tests pour POST /api/persistance/feedbacks ====================

    @Test
    @DisplayName("POST /api/persistance/feedbacks - avec données valides, devrait créer le feedback")
    void createFeedback_avecDonneesValides_devraitCreerFeedback() throws Exception {
        // Given
        FeedbackDTO newDTO = new FeedbackDTO();
        newDTO.setEvaluation(4);
        newDTO.setCommentaire("Bon");
        newDTO.setUtilisateurId(1L);
        newDTO.setRecetteId(1L);

        Feedback savedFeedback = new Feedback();
        savedFeedback.setId(2L);
        savedFeedback.setEvaluation(4);
        savedFeedback.setCommentaire("Bon");

        FeedbackDTO savedDTO = new FeedbackDTO();
        savedDTO.setId(2L);
        savedDTO.setEvaluation(4);
        savedDTO.setCommentaire("Bon");

        when(utilisateurService.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(recetteService.findById(1L)).thenReturn(Optional.of(recette));
        when(feedbackService.existsByUtilisateurIdAndRecetteId(1L, 1L)).thenReturn(false);
        when(feedbackMapper.toEntity(any(FeedbackDTO.class))).thenReturn(savedFeedback);
        when(feedbackService.save(any(Feedback.class), eq(1L), eq(1L))).thenReturn(savedFeedback);
        when(feedbackMapper.toDTO(savedFeedback)).thenReturn(savedDTO);

        // When & Then
        mockMvc.perform(post("/api/persistance/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.evaluation").value(4));

        verify(feedbackService, times(1)).save(any(Feedback.class), eq(1L), eq(1L));
    }

    @Test
    @DisplayName("POST /api/persistance/feedbacks - sans utilisateurId, devrait retourner 400")
    void createFeedback_sansUtilisateurId_devraitRetourner400() throws Exception {
        // Given
        FeedbackDTO dto = new FeedbackDTO();
        dto.setEvaluation(5);
        dto.setRecetteId(1L);

        // When & Then
        mockMvc.perform(post("/api/persistance/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("L'ID de l'utilisateur est obligatoire"));

        verify(feedbackService, never()).save(any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/persistance/feedbacks - sans recetteId, devrait retourner 400")
    void createFeedback_sansRecetteId_devraitRetourner400() throws Exception {
        // Given
        FeedbackDTO dto = new FeedbackDTO();
        dto.setEvaluation(5);
        dto.setUtilisateurId(1L);

        // When & Then
        mockMvc.perform(post("/api/persistance/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("L'ID de la recette est obligatoire"));

        verify(feedbackService, never()).save(any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/persistance/feedbacks - avec évaluation invalide, devrait retourner 400")
    void createFeedback_avecEvaluationInvalide_devraitRetourner400() throws Exception {
        // Given
        FeedbackDTO dto = new FeedbackDTO();
        dto.setEvaluation(6); // > 5
        dto.setUtilisateurId(1L);
        dto.setRecetteId(1L);

        when(utilisateurService.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(recetteService.findById(1L)).thenReturn(Optional.of(recette));

        // When & Then
        mockMvc.perform(post("/api/persistance/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("L'évaluation doit être comprise entre 1 et 5 étoiles"));

        verify(feedbackService, never()).save(any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/persistance/feedbacks - avec feedback déjà existant, devrait retourner 409")
    void createFeedback_avecFeedbackDejaExistant_devraitRetourner409() throws Exception {
        // Given
        FeedbackDTO dto = new FeedbackDTO();
        dto.setEvaluation(4);
        dto.setUtilisateurId(1L);
        dto.setRecetteId(1L);

        when(utilisateurService.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(recetteService.findById(1L)).thenReturn(Optional.of(recette));
        when(feedbackService.existsByUtilisateurIdAndRecetteId(1L, 1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/persistance/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Vous avez déjà noté cette recette."));

        verify(feedbackService, never()).save(any(), any(), any());
    }

    // ==================== Tests pour PUT /api/persistance/feedbacks/{id} ====================

    @Test
    @DisplayName("PUT /api/persistance/feedbacks/{id} - avec données valides, devrait mettre à jour")
    void updateFeedback_avecDonneesValides_devraitMettreAJour() throws Exception {
        // Given
        FeedbackDTO updateDTO = new FeedbackDTO();
        updateDTO.setEvaluation(3);
        updateDTO.setCommentaire("Moyen");

        Feedback updatedFeedback = new Feedback();
        updatedFeedback.setId(1L);
        updatedFeedback.setEvaluation(3);
        updatedFeedback.setCommentaire("Moyen");

        FeedbackDTO updatedDTO = new FeedbackDTO();
        updatedDTO.setId(1L);
        updatedDTO.setEvaluation(3);
        updatedDTO.setCommentaire("Moyen");

        when(feedbackService.findById(1L)).thenReturn(Optional.of(feedback));
        when(feedbackMapper.toEntity(any(FeedbackDTO.class))).thenReturn(updatedFeedback);
        when(feedbackService.update(eq(1L), any(Feedback.class))).thenReturn(updatedFeedback);
        when(feedbackMapper.toDTO(updatedFeedback)).thenReturn(updatedDTO);

        // When & Then
        mockMvc.perform(put("/api/persistance/feedbacks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.evaluation").value(3));

        verify(feedbackService, times(1)).update(eq(1L), any(Feedback.class));
    }

    // ==================== Tests pour DELETE /api/persistance/feedbacks/{id} ====================

    @Test
    @DisplayName("DELETE /api/persistance/feedbacks/{id} - avec ID existant, devrait supprimer")
    void deleteFeedback_avecIdExistant_devraitSupprimer() throws Exception {
        // Given
        when(feedbackService.findById(1L)).thenReturn(Optional.of(feedback));
        doNothing().when(feedbackService).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/api/persistance/feedbacks/1"))
                .andExpect(status().isNoContent());

        verify(feedbackService, times(1)).deleteById(1L);
    }
}

