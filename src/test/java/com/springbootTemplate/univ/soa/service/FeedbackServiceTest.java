package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.exception.ResourceNotFoundException;
import com.springbootTemplate.univ.soa.model.Feedback;
import com.springbootTemplate.univ.soa.model.Recette;
import com.springbootTemplate.univ.soa.model.Utilisateur;
import com.springbootTemplate.univ.soa.repository.FeedbackRepository;
import com.springbootTemplate.univ.soa.repository.RecetteRepository;
import com.springbootTemplate.univ.soa.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour FeedbackService")
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private RecetteRepository recetteRepository;

    @Mock
    private ActiviteService activiteService;

    @InjectMocks
    private FeedbackService feedbackService;

    private Feedback feedback;
    private Utilisateur utilisateur;
    private Recette recette;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setEmail("user@test.com");
        utilisateur.setNom("Dupont");

        recette = new Recette();
        recette.setId(1L);
        recette.setTitre("Pâtes carbonara");

        feedback = new Feedback();
        feedback.setId(1L);
        feedback.setEvaluation(5);
        feedback.setCommentaire("Excellente recette !");
        feedback.setUtilisateur(utilisateur);
        feedback.setRecette(recette);
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("findAll - devrait retourner tous les feedbacks")
    void findAll_devraitRetournerTousLesFeedbacks() {
        // Given
        Feedback feedback2 = new Feedback();
        feedback2.setId(2L);
        feedback2.setEvaluation(4);
        feedback2.setCommentaire("Très bon");

        List<Feedback> feedbacks = Arrays.asList(feedback, feedback2);
        when(feedbackRepository.findAll()).thenReturn(feedbacks);

        // When
        List<Feedback> result = feedbackService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(5, result.get(0).getEvaluation());
        assertEquals(4, result.get(1).getEvaluation());
        verify(feedbackRepository, times(1)).findAll();
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("findById - avec ID existant, devrait retourner le feedback")
    void findById_avecIdExistant_devraitRetournerFeedback() {
        // Given
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

        // When
        Optional<Feedback> result = feedbackService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(5, result.get().getEvaluation());
        assertEquals("Excellente recette !", result.get().getCommentaire());
        verify(feedbackRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById - avec ID inexistant, devrait retourner Optional vide")
    void findById_avecIdInexistant_devraitRetournerOptionalVide() {
        // Given
        when(feedbackRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Feedback> result = feedbackService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(feedbackRepository, times(1)).findById(999L);
    }

    // ==================== Tests pour findByUtilisateurId() ====================

    @Test
    @DisplayName("findByUtilisateurId - devrait retourner les feedbacks de l'utilisateur")
    void findByUtilisateurId_devraitRetournerFeedbacksUtilisateur() {
        // Given
        List<Feedback> feedbacks = Collections.singletonList(feedback);
        when(feedbackRepository.findByUtilisateur_Id(1L)).thenReturn(feedbacks);

        // When
        List<Feedback> result = feedbackService.findByUtilisateurId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        Feedback feedbackResult = result.get(0);
        assertEquals(1L, feedbackResult.getUtilisateur().getId());
        verify(feedbackRepository, times(1)).findByUtilisateur_Id(1L);
    }

    // ==================== Tests pour findByRecetteId() ====================

    @Test
    @DisplayName("findByRecetteId - devrait retourner les feedbacks de la recette")
    void findByRecetteId_devraitRetournerFeedbacksRecette() {
        // Given
        List<Feedback> feedbacks = Collections.singletonList(feedback);
        when(feedbackRepository.findByRecette_Id(1L)).thenReturn(feedbacks);

        // When
        List<Feedback> result = feedbackService.findByRecetteId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        Feedback feedbackResult = result.get(0);
        assertEquals(1L, feedbackResult.getRecette().getId());
        verify(feedbackRepository, times(1)).findByRecette_Id(1L);
    }

    // ==================== Tests pour save() ====================

    @Test
    @DisplayName("save - avec données valides, devrait créer feedback")
    void save_avecDonneesValides_devraitCreerFeedback() {
        // Given
        Feedback nouveauFeedback = new Feedback();
        nouveauFeedback.setEvaluation(4);
        nouveauFeedback.setCommentaire("Bon");

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(recetteRepository.findByIdSimple(anyLong())).thenReturn(Optional.of(recette));
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
            Feedback f = invocation.getArgument(0);
            f.setId(10L);
            return f;
        });
        when(feedbackRepository.calculateAverageEvaluationByRecetteId(1L)).thenReturn(Optional.of(4.5));

        // When
        Feedback result = feedbackService.save(nouveauFeedback, 1L, 1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(10L, result.getId());
        assertEquals(4, result.getEvaluation());
        assertEquals("Bon", result.getCommentaire());
        assertNotNull(result.getUtilisateur());
        assertNotNull(result.getRecette());
        verify(utilisateurRepository, times(1)).findById(1L);
        verify(recetteRepository, times(2)).findByIdSimple(1L);
        verify(feedbackRepository, times(1)).save(any(Feedback.class));
    }

    @Test
    @DisplayName("save - avec utilisateur inexistant, devrait lancer ResourceNotFoundException")
    void save_avecUtilisateurInexistant_devraitLancerException() {
        // Given
        Feedback nouveauFeedback = new Feedback();
        nouveauFeedback.setEvaluation(4);

        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> feedbackService.save(nouveauFeedback, 999L, 1L)
        );

        assertEquals("Utilisateur non trouvé avec l'ID: 999", exception.getMessage());
        verify(utilisateurRepository, times(1)).findById(999L);
        verify(recetteRepository, never()).findByIdSimple(any());
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("save - avec recette inexistante, devrait lancer ResourceNotFoundException")
    void save_avecRecetteInexistante_devraitLancerException() {
        // Given
        Feedback nouveauFeedback = new Feedback();
        nouveauFeedback.setEvaluation(4);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(recetteRepository.findByIdSimple(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> feedbackService.save(nouveauFeedback, 1L, 999L)
        );

        assertEquals("Recette non trouvée avec l'ID: 999", exception.getMessage());
        verify(utilisateurRepository, times(1)).findById(1L);
        verify(recetteRepository, times(1)).findByIdSimple(999L);
        verify(feedbackRepository, never()).save(any());
    }

    // ==================== Tests pour update() ====================

    @Test
    @DisplayName("update - avec ID existant, devrait mettre à jour le feedback")
    void update_avecIdExistant_devraitMettreAJourFeedback() {
        // Given
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(recetteRepository.findByIdSimple(1L)).thenReturn(Optional.of(recette));
        when(feedbackRepository.calculateAverageEvaluationByRecetteId(1L)).thenReturn(Optional.of(5.0));
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Feedback updatedFeedback = new Feedback();
        updatedFeedback.setEvaluation(3);
        updatedFeedback.setCommentaire("Bien mais peut mieux faire");
        updatedFeedback.setRecette(recette);

        // When
        Feedback result = feedbackService.update(1L, updatedFeedback);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getEvaluation());
        assertEquals("Bien mais peut mieux faire", result.getCommentaire());
        verify(feedbackRepository, times(1)).save(any(Feedback.class));
        verify(recetteRepository, times(1)).save(recette);
    }

    @Test
    @DisplayName("update - avec ID inexistant, devrait lancer ResourceNotFoundException")
    void update_avecIdInexistant_devraitLancerException() {
        // Given
        Feedback feedbackMisAJour = new Feedback();
        feedbackMisAJour.setEvaluation(3);

        when(feedbackRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> feedbackService.update(999L, feedbackMisAJour)
        );

        assertEquals("Feedback non trouvé avec l'ID: 999", exception.getMessage());
        verify(feedbackRepository, times(1)).findById(999L);
        verify(feedbackRepository, never()).save(any());
    }

    // ==================== Tests pour deleteById() ====================

    @Test
    @DisplayName("deleteById - avec ID existant, devrait supprimer le feedback")
    void deleteById_avecIdExistant_devraitSupprimerFeedback() {
        // Given
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(recetteRepository.findByIdSimple(1L)).thenReturn(Optional.of(recette));
        when(feedbackRepository.calculateAverageEvaluationByRecetteId(1L)).thenReturn(Optional.of(4.0));

        // When
        feedbackService.deleteById(1L);

        // Then
        verify(feedbackRepository, times(1)).deleteById(1L);
        verify(recetteRepository, times(1)).save(recette);
    }

    @Test
    @DisplayName("deleteById - avec ID inexistant, devrait lancer ResourceNotFoundException")
    void deleteById_avecIdInexistant_devraitLancerException() {
        // Given
        when(feedbackRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> feedbackService.deleteById(999L));
        verify(feedbackRepository, never()).deleteById(any());
    }
}
