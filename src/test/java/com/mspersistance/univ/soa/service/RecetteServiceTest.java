package com.mspersistance.univ.soa.service;


import com.mspersistance.univ.soa.dto.RecetteDTO;
import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.factory.RecetteFactory;
import com.mspersistance.univ.soa.model.*;
import com.mspersistance.univ.soa.repository.AlimentRepository;
import com.mspersistance.univ.soa.repository.NotificationRepository;
import com.mspersistance.univ.soa.repository.RecetteRepository;
import com.mspersistance.univ.soa.repository.UtilisateurRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour RecetteService")
class RecetteServiceTest {

    @Mock
    private RecetteRepository recetteRepository;

    @Mock
    private AlimentRepository alimentRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ActiviteService activiteService;

    @Mock
    private RecetteFactory recetteFactory;

    @InjectMocks
    private RecetteService recetteService;

    private Recette recette;
    private Aliment aliment;
    private Ingredient ingredient;
    private Etape etape;

    @BeforeEach
    void setUp() {
        // Aliment
        aliment = new Aliment();
        aliment.setId(1L);
        aliment.setNom("Tomate");
        aliment.setCategorieAliment(Aliment.CategorieAliment.LEGUME);

        // Ingredient
        ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setAliment(aliment);
        ingredient.setQuantite(200.0f);
        ingredient.setUnite(Ingredient.Unite.GRAMME);
        ingredient.setPrincipal(true);

        // Etape
        etape = new Etape();
        etape.setId(1L);
        etape.setOrdre(1);
        etape.setTemps(10);
        etape.setTexte("Couper les tomates");

        // Recette
        recette = new Recette();
        recette.setId(1L);
        recette.setTitre("Salade de tomates");
        recette.setTempsTotal(15);
        recette.setKcal(150);
        recette.setImageUrl("http://example.com/image.jpg");
        recette.setDifficulte(Recette.Difficulte.FACILE);
        recette.setIngredients(new ArrayList<>(Arrays.asList(ingredient)));
        recette.setEtapes(new ArrayList<>(Arrays.asList(etape)));

        ingredient.setRecette(recette);
        etape.setRecette(recette);
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("findAll - devrait retourner toutes les recettes")
    void findAll_devraitRetournerToutesLesRecettes() {
        // Given
        Recette recette2 = new Recette();
        recette2.setId(2L);
        recette2.setTitre("Soupe");

        when(recetteRepository.findAllOptimized()).thenReturn(Arrays.asList(recette, recette2));

        // When
        List<Recette> result = recetteService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(recetteRepository, times(1)).findAllOptimized();
    }

    @Test
    @DisplayName("findById - avec ID existant, devrait retourner la recette")
    void findById_avecIdExistant_devraitRetournerRecette() {
        // Given
        when(recetteRepository.findByIdOptimized(1L)).thenReturn(Optional.of(recette));

        // When
        Optional<Recette> result = recetteService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Salade de tomates", result.get().getTitre());
        verify(recetteRepository, times(1)).findByIdOptimized(1L);
    }

    @Test
    @DisplayName("findById - avec ID inexistant, devrait retourner Optional vide")
    void findById_avecIdInexistant_devraitRetournerOptionalVide() {
        // Given
        when(recetteRepository.findByIdOptimized(999L)).thenReturn(Optional.empty());

        // When
        Optional<Recette> result = recetteService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(recetteRepository, times(1)).findByIdOptimized(999L);
    }

    // ==================== Tests pour save() ====================

    @Test
    @DisplayName("save - avec recette valide, devrait crÃ©er la recette")
    void save_avecRecetteValide_devraitCreerRecette() {
        // Given
        Recette nouvelleRecette = new Recette();
        nouvelleRecette.setTitre("Soupe de lÃ©gumes");
        nouvelleRecette.setTempsTotal(30);
        nouvelleRecette.setKcal(200);
        nouvelleRecette.setDifficulte(Recette.Difficulte.FACILE);
        nouvelleRecette.setIngredients(new ArrayList<>());
        nouvelleRecette.setEtapes(new ArrayList<>());

        Ingredient newIngredient = new Ingredient();
        newIngredient.setAliment(aliment);
        newIngredient.setQuantite(100.0f);
        nouvelleRecette.getIngredients().add(newIngredient);

        Etape newEtape = new Etape();
        newEtape.setOrdre(1);
        newEtape.setTexte("Faire bouillir");
        nouvelleRecette.getEtapes().add(newEtape);

        Recette recetteSauvegardee = new Recette();
        recetteSauvegardee.setId(2L);
        recetteSauvegardee.setTitre("Soupe de lÃ©gumes");
        recetteSauvegardee.setIngredients(new ArrayList<>());
        recetteSauvegardee.setEtapes(new ArrayList<>());

        when(alimentRepository.findById(1L)).thenReturn(Optional.of(aliment));
        when(recetteRepository.save(any(Recette.class))).thenReturn(recetteSauvegardee);

        // When
        Recette result = recetteService.save(nouvelleRecette);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Soupe de lÃ©gumes", result.getTitre());
        verify(alimentRepository, times(1)).findById(1L);
        verify(recetteRepository, times(1)).save(any(Recette.class));
    }

    @Test
    @DisplayName("save - avec aliment inexistant, devrait lancer ResourceNotFoundException")
    void save_avecAlimentInexistant_devraitLancerException() {
        // Given
        Recette nouvelleRecette = new Recette();
        nouvelleRecette.setTitre("Test");
        nouvelleRecette.setIngredients(new ArrayList<>());

        Ingredient newIngredient = new Ingredient();
        Aliment alimentInexistant = new Aliment();
        alimentInexistant.setId(999L);
        newIngredient.setAliment(alimentInexistant);
        nouvelleRecette.getIngredients().add(newIngredient);

        when(alimentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> recetteService.save(nouvelleRecette)
        );

        assertEquals("Aliment non trouvÃ© avec l'ID: 999", exception.getMessage());
        verify(alimentRepository, times(1)).findById(999L);
        verify(recetteRepository, never()).save(any());
    }

    @Test
    @DisplayName("save - devrait mettre l'ID de la recette Ã  null")
    void save_devraitMettreIdRecetteANull() {
        // Given
        Recette recetteAvecId = new Recette();
        recetteAvecId.setId(999L);
        recetteAvecId.setTitre("Test");
        recetteAvecId.setIngredients(new ArrayList<>());
        recetteAvecId.setEtapes(new ArrayList<>());

        when(recetteRepository.save(any(Recette.class))).thenReturn(recette);

        // When
        recetteService.save(recetteAvecId);

        // Then
        assertNull(recetteAvecId.getId());
        verify(recetteRepository, times(1)).save(any(Recette.class));
    }

    // ==================== Tests pour update() ====================

    @Test
    @DisplayName("update - avec ID existant, devrait mettre Ã  jour la recette")
    void update_avecIdExistant_devraitMettreAJourRecette() {
        // Given
        Recette recetteMiseAJour = new Recette();
        recetteMiseAJour.setTitre("Salade amÃ©liorÃ©e");
        recetteMiseAJour.setTempsTotal(20);
        recetteMiseAJour.setKcal(180);
        recetteMiseAJour.setImageUrl("http://example.com/new.jpg");
        recetteMiseAJour.setDifficulte(Recette.Difficulte.MOYEN);
        recetteMiseAJour.setIngredients(new ArrayList<>());
        recetteMiseAJour.setEtapes(new ArrayList<>());

        when(recetteRepository.findById(1L)).thenReturn(Optional.of(recette));
        when(recetteRepository.save(any(Recette.class))).thenReturn(recette);

        // When
        Recette result = recetteService.update(1L, recetteMiseAJour);

        // Then
        assertNotNull(result);
        assertEquals("Salade amÃ©liorÃ©e", result.getTitre());
        assertEquals(20, result.getTempsTotal());
        assertEquals(180, result.getKcal());
        verify(recetteRepository, times(1)).findById(1L);
        verify(recetteRepository, times(1)).save(recette);
    }

    @Test
    @DisplayName("update - avec ID inexistant, devrait lancer ResourceNotFoundException")
    void update_avecIdInexistant_devraitLancerException() {
        // Given
        Recette recetteMiseAJour = new Recette();
        recetteMiseAJour.setTitre("Test");

        when(recetteRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> recetteService.update(999L, recetteMiseAJour)
        );

        assertEquals("Recette non trouvÃ©e avec l'ID: 999", exception.getMessage());
        verify(recetteRepository, times(1)).findById(999L);
        verify(recetteRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - devrait remplacer les ingrÃ©dients existants")
    void update_devraitRemplacerIngredientsExistants() {
        // Given
        Recette recetteMiseAJour = new Recette();
        recetteMiseAJour.setTitre("Test");
        recetteMiseAJour.setIngredients(new ArrayList<>());

        Ingredient nouvelIngredient = new Ingredient();
        nouvelIngredient.setAliment(aliment);
        nouvelIngredient.setQuantite(300.0f);
        recetteMiseAJour.getIngredients().add(nouvelIngredient);

        when(recetteRepository.findById(1L)).thenReturn(Optional.of(recette));
        when(alimentRepository.findById(1L)).thenReturn(Optional.of(aliment));
        when(recetteRepository.save(any(Recette.class))).thenReturn(recette);

        // When
        Recette result = recetteService.update(1L, recetteMiseAJour);

        // Then
        assertNotNull(result);
        verify(recetteRepository, times(1)).findById(1L);
        verify(alimentRepository, times(1)).findById(1L);
        verify(recetteRepository, times(1)).save(recette);
    }

    // ==================== Tests pour saveFromDTO() ====================

    @Test
    @DisplayName("saveFromDTO - avec ingrÃ©dient utilisant alimentId, devrait crÃ©er la recette")
    void saveFromDTO_avecIngredientUtilisantAlimentId_devraitCreerRecette() {
        // Given - Utiliser les constructeurs Records
        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(
            null,      // id
            1L,        // alimentId
            "Tomate",  // alimentNom
            null,      // nomAliment
            200.0f,    // quantite
            "GRAMME",  // unite
            true       // principal
        );

        RecetteDTO dto = new RecetteDTO(
            null,                              // id
            "Salade",                          // titre
            "Une bonne salade",                // description
            15,                                // tempsTotal
            null,                              // kcal
            null,                              // imageUrl
            null,                              // difficulte
            null,                              // dateCreation
            null,                              // dateModification
            null,                              // actif
            null,                              // statut
            null,                              // motifRejet
            1L,                                // utilisateurId
            null,                              // moyenneEvaluation
            Arrays.asList(ingredientDTO),      // ingredients
            null                               // etapes
        );

        // Mock RecetteFactory pour retourner une recette valide
        Recette recetteCreee = new Recette();
        recetteCreee.setTitre("Salade");
        recetteCreee.setDescription("Une bonne salade");
        recetteCreee.setTempsTotal(15);
        recetteCreee.setUtilisateurId(1L);

        Ingredient ing = new Ingredient();
        ing.setAliment(aliment);
        ing.setQuantite(200.0f);
        ing.setUnite(Ingredient.Unite.GRAMME);
        ing.setPrincipal(true);
        ing.setRecette(recetteCreee);
        recetteCreee.setIngredients(new ArrayList<>(Arrays.asList(ing)));

        when(recetteFactory.createFromDTO(any(RecetteDTO.class))).thenReturn(recetteCreee);
        when(recetteRepository.save(any(Recette.class))).thenAnswer(i -> {
            Recette r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        // When
        Recette result = recetteService.saveFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals("Salade", result.getTitre());
        assertEquals(1, result.getIngredients().size());
        assertEquals(aliment, result.getIngredients().get(0).getAliment());
        verify(recetteFactory, times(1)).createFromDTO(any(RecetteDTO.class));
        verify(recetteRepository, times(1)).save(any(Recette.class));
    }

    @Test
    @DisplayName("saveFromDTO - avec ingrÃ©dient utilisant nomAliment, devrait crÃ©er la recette")
    void saveFromDTO_avecIngredientUtilisantNomAliment_devraitCreerRecette() {
        // Given - Utiliser les constructeurs Records
        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(
            null,              // id
            null,              // alimentId
            "Tomate cerise",   // alimentNom
            "Tomate cerise",   // nomAliment
            150.0f,            // quantite
            null,              // unite
            true               // principal
        );

        RecetteDTO dto = new RecetteDTO(
            null, "Salade", "Une bonne salade", 15, null, null, null,
            null, null, null, null, null, 1L, null,
            Arrays.asList(ingredientDTO), null
        );

        // Mock RecetteFactory
        Recette recetteCreee = new Recette();
        recetteCreee.setTitre("Salade");
        recetteCreee.setUtilisateurId(1L);

        Aliment nouvelAliment = new Aliment();
        nouvelAliment.setId(2L);
        nouvelAliment.setNom("Tomate cerise");

        Ingredient ing = new Ingredient();
        ing.setAliment(nouvelAliment);
        ing.setNomAliment("Tomate cerise");
        ing.setQuantite(150.0f);
        ing.setPrincipal(true);
        ing.setRecette(recetteCreee);
        recetteCreee.setIngredients(new ArrayList<>(Arrays.asList(ing)));

        when(recetteFactory.createFromDTO(any(RecetteDTO.class))).thenReturn(recetteCreee);
        when(recetteRepository.save(any(Recette.class))).thenAnswer(i -> {
            Recette r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        // When
        Recette result = recetteService.saveFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals("Salade", result.getTitre());
        assertEquals(1, result.getIngredients().size());
        assertEquals("Tomate cerise", result.getIngredients().get(0).getNomAliment());
        verify(recetteFactory, times(1)).createFromDTO(any(RecetteDTO.class));
        verify(recetteRepository, times(1)).save(any(Recette.class));
    }

    @Test
    @DisplayName("saveFromDTO - sans alimentId ni nomAliment, devrait lancer exception")
    void saveFromDTO_sansAlimentIdNiNomAliment_devraitLancerException() {
        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(
            null, null, null, null, 150.0f, null, false
        );

        RecetteDTO dto = new RecetteDTO(
            null, "Salade", null, null, null, null, null, null, null,
            null, null, null, null, null, Arrays.asList(ingredientDTO), null
        );

        when(recetteFactory.createFromDTO(any(RecetteDTO.class)))
            .thenThrow(new IllegalArgumentException("L'ID ou le nom de l'aliment est requis"));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> recetteService.saveFromDTO(dto)
        );

        assertEquals("L'ID ou le nom de l'aliment est requis", exception.getMessage());
        verify(recetteRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveFromDTO - avec alimentNom existant, devrait rÃ©utiliser l'aliment")
    void saveFromDTO_avecAlimentNomExistant_devraitReutiliserAliment() {
        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(
            null, null, "Tomate", "Tomate", 100.0f, null, true
        );

        RecetteDTO dto = new RecetteDTO(
            null, "Salade aux tomates", null, null, null, null, null,
            null, null, null, null, null, 1L, null,
            Arrays.asList(ingredientDTO), null
        );

        Aliment alimentExistant = new Aliment();
        alimentExistant.setId(50L);
        alimentExistant.setNom("Tomate");
        alimentExistant.setCalories(20f);
        alimentExistant.setCategorieAliment(Aliment.CategorieAliment.LEGUME);

        Recette recetteCreee = new Recette();
        recetteCreee.setTitre("Salade aux tomates");
        recetteCreee.setUtilisateurId(1L);

        Ingredient ing = new Ingredient();
        ing.setAliment(alimentExistant);
        ing.setQuantite(100.0f);
        ing.setRecette(recetteCreee);
        recetteCreee.setIngredients(new ArrayList<>(Arrays.asList(ing)));

        when(recetteFactory.createFromDTO(any(RecetteDTO.class))).thenReturn(recetteCreee);
        when(recetteRepository.save(any(Recette.class))).thenAnswer(i -> {
            Recette r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        Recette result = recetteService.saveFromDTO(dto);

        assertNotNull(result);
        assertEquals(1, result.getIngredients().size());
        assertEquals(50L, result.getIngredients().get(0).getAliment().getId());
        assertEquals("Tomate", result.getIngredients().get(0).getAliment().getNom());
        verify(recetteFactory, times(1)).createFromDTO(any(RecetteDTO.class));
        verify(recetteRepository, times(1)).save(any(Recette.class));
    }

    @Test
    @DisplayName("deleteById - avec ID existant, devrait supprimer la recette")
    void deleteById_avecIdExistant_devraitSupprimerRecette() {
        // Given
        when(recetteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(recetteRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> recetteService.deleteById(1L));

        // Then
        verify(recetteRepository, times(1)).existsById(1L);
        verify(recetteRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById - avec ID inexistant, devrait lancer ResourceNotFoundException")
    void deleteById_avecIdInexistant_devraitLancerException() {
        // Given
        when(recetteRepository.existsById(999L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> recetteService.deleteById(999L)
        );

        assertEquals("Recette non trouvÃ©e avec l'ID: 999", exception.getMessage());
        verify(recetteRepository, times(1)).existsById(999L);
        verify(recetteRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("saveFromDTO - devrait logger l'activitÃ©")
    void saveFromDTO_devraitLoggerActivite() {
        RecetteDTO.IngredientDTO ing = new RecetteDTO.IngredientDTO(
            null, null, "Tomate", "Tomate", 100f, null, true
        );

        RecetteDTO dto = new RecetteDTO(
            null, "Salade", null, null, null, null, null, null, null,
            null, null, null, 1L, null, new ArrayList<>(List.of(ing)), null
        );

        Recette recetteCreee = new Recette();
        recetteCreee.setTitre("Salade");
        recetteCreee.setUtilisateurId(1L);
        recetteCreee.setIngredients(new ArrayList<>());

        when(recetteFactory.createFromDTO(any(RecetteDTO.class))).thenReturn(recetteCreee);
        when(recetteRepository.save(any(Recette.class))).thenAnswer(invocation -> {
            Recette r = invocation.getArgument(0);
            r.setId(20L);
            return r;
        });

        Recette saved = recetteService.saveFromDTO(dto);

        assertNotNull(saved.getId());
        verify(activiteService, times(1)).logActivite(eq(1L), eq(Activite.TypeActivite.RECETTE_CREEE), anyString());
    }
}
