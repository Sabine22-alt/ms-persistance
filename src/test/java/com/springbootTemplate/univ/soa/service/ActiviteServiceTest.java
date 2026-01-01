package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.model.Activite;
import com.springbootTemplate.univ.soa.repository.ActiviteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour ActiviteService")
class ActiviteServiceTest {

    @Mock
    private ActiviteRepository activiteRepository;

    @InjectMocks
    private ActiviteService activiteService;

    private Activite activite;

    @BeforeEach
    void setUp() {
        activite = new Activite();
        activite.setId(1L);
        activite.setUtilisateurId(1L);
        activite.setType(Activite.TypeActivite.RECETTE_CREEE);
        activite.setDescription("Créée une recette");
    }

    @Test
    @DisplayName("logActivite - devrait enregistrer une activité")
    void logActivite_devraitEnregistrerActivite() {
        // Given
        when(activiteRepository.save(any(Activite.class))).thenAnswer(invocation -> {
            Activite a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        // When
        Activite result = activiteService.logActivite(1L, Activite.TypeActivite.RECETTE_CREEE, "Test");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(Activite.TypeActivite.RECETTE_CREEE, result.getType());
        verify(activiteRepository, times(1)).save(any(Activite.class));
    }

    @Test
    @DisplayName("logActiviteWithDetails - devrait enregistrer une activité avec détails")
    void logActiviteWithDetails_devraitEnregistrerActiviteAvecDetails() {
        // Given
        when(activiteRepository.save(any(Activite.class))).thenAnswer(invocation -> {
            Activite a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        // When
        Activite result = activiteService.logActiviteWithDetails(
            1L, Activite.TypeActivite.RECETTE_CREEE, "Test", "{\"key\":\"value\"}"
        );

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("{\"key\":\"value\"}", result.getDetails());
        verify(activiteRepository, times(1)).save(any(Activite.class));
    }

    @Test
    @DisplayName("getActivitesByUtilisateur - devrait retourner les activités d'un utilisateur")
    void getActivitesByUtilisateur_devraitRetournerActivites() {
        // Given
        List<Activite> activites = Arrays.asList(activite);
        when(activiteRepository.findByUtilisateurIdOrderByDateActivityDesc(1L)).thenReturn(activites);

        // When
        List<Activite> result = activiteService.getActivitesByUtilisateur(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(activiteRepository, times(1)).findByUtilisateurIdOrderByDateActivityDesc(1L);
    }

    @Test
    @DisplayName("getLast10Activites - devrait retourner les 10 dernières activités")
    void getLast10Activites_devraitRetournerDernieresActivites() {
        // Given
        List<Activite> activites = Arrays.asList(activite);
        when(activiteRepository.findLast10ActivitiesByUtilisateurId(1L)).thenReturn(activites);

        // When
        List<Activite> result = activiteService.getLast10Activites(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(activiteRepository, times(1)).findLast10ActivitiesByUtilisateurId(1L);
    }
}

