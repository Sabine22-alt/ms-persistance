package com.mspersistance.univ.soa.factory;

import com.mspersistance.univ.soa.dto.FeedbackDTO;
import com.mspersistance.univ.soa.model.Feedback;
import com.mspersistance.univ.soa.model.Recette;
import com.mspersistance.univ.soa.model.Utilisateur;
import com.mspersistance.univ.soa.repository.RecetteRepository;
import com.mspersistance.univ.soa.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour FeedbackFactory")
class FeedbackFactoryTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private RecetteRepository recetteRepository;

    @InjectMocks
    private FeedbackFactory feedbackFactory;

    private Utilisateur utilisateur;
    private Recette recette;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setEmail("john@test.com");

        recette = new Recette();
        recette.setId(1L);
        recette.setTitre("Salade");
    }

    @Test
    @DisplayName("createFromDTO - avec DTO valide, devrait créer feedback")
    void createFromDTO_avecDTOValide_devraitCreerFeedback() {
        FeedbackDTO dto = new FeedbackDTO(null, 1L, 1L, 5, "Excellent!", null, null);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(recetteRepository.findByIdSimple(1L)).thenReturn(Optional.of(recette));

        Feedback feedback = feedbackFactory.createFromDTO(dto);

        assertNotNull(feedback);
        assertEquals(utilisateur, feedback.getUtilisateur());
        assertEquals(recette, feedback.getRecette());
        assertEquals(5, feedback.getEvaluation());
        assertEquals("Excellent!", feedback.getCommentaire());
        verify(utilisateurRepository, times(1)).findById(1L);
        verify(recetteRepository, times(1)).findByIdSimple(1L);
    }

    @Test
    @DisplayName("createFromDTO - avec commentaire null, devrait fonctionner")
    void createFromDTO_avecCommentaireNull_devraitFonctionner() {
        FeedbackDTO dto = new FeedbackDTO(null, 1L, 1L, 4, null, null, null);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(recetteRepository.findByIdSimple(1L)).thenReturn(Optional.of(recette));

        Feedback feedback = feedbackFactory.createFromDTO(dto);

        assertNotNull(feedback);
        assertNull(feedback.getCommentaire());
        assertEquals(4, feedback.getEvaluation());
    }

    @Test
    @DisplayName("createFromDTO - avec utilisateur inexistant, devrait lancer exception")
    void createFromDTO_avecUtilisateurInexistant_devraitLancerException() {
        FeedbackDTO dto = new FeedbackDTO(null, 999L, 1L, 5, "Test", null, null);

        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> feedbackFactory.createFromDTO(dto));
        verify(recetteRepository, never()).findByIdSimple(any());
    }

    @Test
    @DisplayName("createFromDTO - avec recette inexistante, devrait lancer exception")
    void createFromDTO_avecRecetteInexistante_devraitLancerException() {
        FeedbackDTO dto = new FeedbackDTO(null, 1L, 999L, 5, "Test", null, null);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(recetteRepository.findByIdSimple(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> feedbackFactory.createFromDTO(dto));
    }

    @Test
    @DisplayName("updateFromDTO - devrait mettre à jour feedback existant")
    void updateFromDTO_devraitMettreAJourFeedbackExistant() {
        Feedback existing = new Feedback();
        existing.setId(1L);
        existing.setEvaluation(3);
        existing.setCommentaire("Ancien commentaire");
        existing.setUtilisateur(utilisateur);
        existing.setRecette(recette);

        FeedbackDTO dto = new FeedbackDTO(null, null, null, 5, "Nouveau commentaire", null, null);

        Feedback updated = feedbackFactory.updateFromDTO(existing, dto);

        assertNotNull(updated);
        assertEquals(1L, updated.getId());
        assertEquals(5, updated.getEvaluation());
        assertEquals("Nouveau commentaire", updated.getCommentaire());
        assertEquals(utilisateur, updated.getUtilisateur());
        assertEquals(recette, updated.getRecette());
    }
}

