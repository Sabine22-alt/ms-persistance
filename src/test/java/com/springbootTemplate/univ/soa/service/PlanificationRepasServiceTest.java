package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.model.PlanificationRepas;
import com.springbootTemplate.univ.soa.model.PlanificationJour;
import com.springbootTemplate.univ.soa.repository.PlanificationRepasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour PlanificationRepasService")
class PlanificationRepasServiceTest {

    @Mock
    private PlanificationRepasRepository planificationRepasRepository;

    @Mock
    private ActiviteService activiteService;

    @InjectMocks
    private PlanificationRepasService planificationRepasService;

    private PlanificationRepas planificationRepas;

    @BeforeEach
    void setUp() {
        planificationRepas = new PlanificationRepas();
        planificationRepas.setId(1L);
        planificationRepas.setUtilisateurId(1L);
        planificationRepas.setSemaine(1);
        planificationRepas.setAnnee(2026);
        planificationRepas.setJours(new ArrayList<>());

        // Initialiser avec 7 jours
        for (int jour = 0; jour < 7; jour++) {
            PlanificationJour pj = new PlanificationJour();
            pj.setJour(jour);
            pj.setPlanification(planificationRepas);
            pj.setRepas(new ArrayList<>());
            planificationRepas.getJours().add(pj);
        }
    }

    @Test
    @DisplayName("getOrCreatePlanification - devrait créer une nouvelle planification si elle n'existe pas")
    void getOrCreatePlanification_devraitCreerNouvellePlanification() {
        // Given
        when(planificationRepasRepository.findByUtilisateurAndWeek(1L, 1, 2026)).thenReturn(Optional.empty());
        when(planificationRepasRepository.save(any(PlanificationRepas.class))).thenReturn(planificationRepas);

        // When
        PlanificationRepas result = planificationRepasService.getOrCreatePlanification(1L, 1, 2026);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUtilisateurId());
        assertEquals(7, result.getJours().size());
        verify(planificationRepasRepository, times(1)).save(any(PlanificationRepas.class));
    }

    @Test
    @DisplayName("getOrCreatePlanification - devrait retourner une planification existante")
    void getOrCreatePlanification_devraitRetournerPlanificationExistante() {
        // Given
        when(planificationRepasRepository.findByUtilisateurAndWeek(1L, 1, 2026))
            .thenReturn(Optional.of(planificationRepas));

        // When
        PlanificationRepas result = planificationRepasService.getOrCreatePlanification(1L, 1, 2026);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(planificationRepasRepository, never()).save(any(PlanificationRepas.class));
    }

    @Test
    @DisplayName("getPlanification - devrait retourner une planification")
    void getPlanification_devraitRetournerPlanification() {
        // Given
        when(planificationRepasRepository.findByUtilisateurAndWeek(1L, 1, 2026))
            .thenReturn(Optional.of(planificationRepas));

        // When
        Optional<PlanificationRepas> result = planificationRepasService.getPlanification(1L, 1, 2026);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(planificationRepasRepository, times(1)).findByUtilisateurAndWeek(1L, 1, 2026);
    }

    @Test
    @DisplayName("addOrUpdateRepas - devrait ajouter un repas")
    void addOrUpdateRepas_devraitAjouterRepas() {
        // Given
        when(planificationRepasRepository.findByUtilisateurAndWeek(1L, 1, 2026))
            .thenReturn(Optional.of(planificationRepas));
        when(planificationRepasRepository.save(any(PlanificationRepas.class))).thenReturn(planificationRepas);

        // When
        PlanificationRepas result = planificationRepasService.addOrUpdateRepas(1L, 1, 2026, 0, 0, 1L, "Note");

        // Then
        assertNotNull(result);
        verify(planificationRepasRepository, times(1)).save(any(PlanificationRepas.class));
    }

    @Test
    @DisplayName("deleteRepas - devrait supprimer un repas")
    void deleteRepas_devraitSupprimerRepas() {
        // Given
        when(planificationRepasRepository.findByUtilisateurAndWeek(1L, 1, 2026))
            .thenReturn(Optional.of(planificationRepas));
        when(planificationRepasRepository.save(any(PlanificationRepas.class))).thenReturn(planificationRepas);

        // When
        PlanificationRepas result = planificationRepasService.deleteRepas(1L, 1, 2026, 0, 0);

        // Then
        assertNotNull(result);
        verify(planificationRepasRepository, times(1)).save(any(PlanificationRepas.class));
    }

    @Test
    @DisplayName("getPlanificationsHistory - devrait retourner l'historique des planifications")
    void getPlanificationsHistory_devraitRetournerHistorique() {
        // Given
        List<PlanificationRepas> planifications = Arrays.asList(planificationRepas);
        when(planificationRepasRepository.findByUtilisateurIdOrderByWeekDesc(1L)).thenReturn(planifications);

        // When
        List<PlanificationRepas> result = planificationRepasService.getPlanificationsHistory(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(planificationRepasRepository, times(1)).findByUtilisateurIdOrderByWeekDesc(1L);
    }
}

