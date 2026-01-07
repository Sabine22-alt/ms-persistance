package com.mspersistance.univ.soa.factory;

import com.mspersistance.univ.soa.dto.RecetteDTO;
import com.mspersistance.univ.soa.model.Aliment;
import com.mspersistance.univ.soa.model.Recette;
import com.mspersistance.univ.soa.repository.AlimentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour RecetteFactory")
class RecetteFactoryTest {

    @Mock
    private AlimentRepository alimentRepository;

    @InjectMocks
    private RecetteFactory recetteFactory;

    private Aliment aliment;

    @BeforeEach
    void setUp() {
        aliment = new Aliment();
        aliment.setId(1L);
        aliment.setNom("Tomate");
        aliment.setCategorieAliment(Aliment.CategorieAliment.LEGUME);
    }

    @Test
    @DisplayName("createFromDTO - avec ingrÃ©dient utilisant alimentId, devrait crÃ©er recette")
    void createFromDTO_avecIngredientUtilisantAlimentId_devraitCreerRecette() {
        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(
            null, 1L, "Tomate", null, 200.0f, "GRAMME", true
        );

        RecetteDTO dto = new RecetteDTO(
            null, "Salade", "Une bonne salade", 15, 100, null,
            Recette.Difficulte.FACILE, null, null, null, null, null, 1L, null,
            Arrays.asList(ingredientDTO), null
        );

        when(alimentRepository.findById(1L)).thenReturn(Optional.of(aliment));

        Recette recette = recetteFactory.createFromDTO(dto);

        assertNotNull(recette);
        assertEquals("Salade", recette.getTitre());
        assertEquals(1, recette.getIngredients().size());
        assertEquals(aliment, recette.getIngredients().get(0).getAliment());
        assertEquals(200.0f, recette.getIngredients().get(0).getQuantite());
        verify(alimentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("createFromDTO - avec ingrÃ©dient utilisant alimentNom existant, devrait rÃ©utiliser aliment")
    void createFromDTO_avecIngredientUtilisantAlimentNomExistant_devraitReutiliserAliment() {
        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(
            null, null, "Tomate", "Tomate", 150.0f, null, true
        );

        RecetteDTO dto = new RecetteDTO(
            null, "Salade", null, 10, null, null, null, null, null,
            null, null, null, 1L, null, Arrays.asList(ingredientDTO), null
        );

        when(alimentRepository.findByNomIgnoreCase("Tomate")).thenReturn(Optional.of(aliment));

        Recette recette = recetteFactory.createFromDTO(dto);

        assertNotNull(recette);
        assertEquals(1, recette.getIngredients().size());
        assertEquals(aliment, recette.getIngredients().get(0).getAliment());
        assertEquals("Tomate", recette.getIngredients().get(0).getNomAliment());
        verify(alimentRepository, times(1)).findByNomIgnoreCase("Tomate");
        verify(alimentRepository, never()).save(any());
    }

    @Test
    @DisplayName("createFromDTO - avec ingrÃ©dient utilisant alimentNom nouveau, devrait crÃ©er aliment")
    void createFromDTO_avecIngredientUtilisantAlimentNomNouveau_devraitCreerAliment() {
        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(
            null, null, "Concombre", "Concombre", 100.0f, null, false
        );

        RecetteDTO dto = new RecetteDTO(
            null, "Salade", null, 10, null, null, null, null, null,
            null, null, null, 1L, null, Arrays.asList(ingredientDTO), null
        );

        Aliment nouveauAliment = new Aliment();
        nouveauAliment.setId(2L);
        nouveauAliment.setNom("Concombre");

        when(alimentRepository.findByNomIgnoreCase("Concombre")).thenReturn(Optional.empty());
        when(alimentRepository.save(any(Aliment.class))).thenReturn(nouveauAliment);

        Recette recette = recetteFactory.createFromDTO(dto);

        assertNotNull(recette);
        assertEquals(1, recette.getIngredients().size());
        assertEquals(nouveauAliment, recette.getIngredients().get(0).getAliment());
        verify(alimentRepository, times(1)).findByNomIgnoreCase("Concombre");
        verify(alimentRepository, times(1)).save(any(Aliment.class));
    }

    @Test
    @DisplayName("createFromDTO - avec Ã©tapes, devrait crÃ©er recette avec Ã©tapes")
    void createFromDTO_avecEtapes_devraitCreerRecetteAvecEtapes() {
        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(
            null, 1L, "Tomate", null, 200.0f, null, true
        );
        RecetteDTO.EtapeDTO etape1 = new RecetteDTO.EtapeDTO(null, 1, 5, "Couper les tomates");
        RecetteDTO.EtapeDTO etape2 = new RecetteDTO.EtapeDTO(null, 2, 3, "Assaisonner");

        RecetteDTO dto = new RecetteDTO(
            null, "Salade", null, 10, null, null, null, null, null,
            null, null, null, 1L, null,
            Arrays.asList(ingredientDTO),
            Arrays.asList(etape1, etape2)
        );

        when(alimentRepository.findById(1L)).thenReturn(Optional.of(aliment));

        Recette recette = recetteFactory.createFromDTO(dto);

        assertNotNull(recette);
        assertEquals(1, recette.getIngredients().size());
        assertEquals(aliment, recette.getIngredients().get(0).getAliment());
        assertEquals(2, recette.getEtapes().size());
        assertEquals(1, recette.getEtapes().get(0).getOrdre());
        assertEquals("Couper les tomates", recette.getEtapes().get(0).getTexte());
        assertEquals(2, recette.getEtapes().get(1).getOrdre());
        verify(alimentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("createFromDTO - sans ingrÃ©dients, devrait lancer exception")
    void createFromDTO_sansIngredients_devraitLancerException() {
        RecetteDTO dto = new RecetteDTO(
            null, "Test", null, 10, null, null, null, null, null,
            null, null, null, 1L, null, new ArrayList<>(), null
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> recetteFactory.createFromDTO(dto)
        );

        assertEquals("Au moins un ingrÃ©dient est requis", exception.getMessage());
    }

    @Test
    @DisplayName("createFromDTO - avec ingrÃ©dient sans alimentId ni nomAliment, devrait lancer exception")
    void createFromDTO_avecIngredientSansAlimentIdNiNomAliment_devraitLancerException() {
        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(
            null, null, null, null, 100.0f, null, null
        );

        RecetteDTO dto = new RecetteDTO(
            null, "Test", null, 10, null, null, null, null, null,
            null, null, null, 1L, null, Arrays.asList(ingredientDTO), null
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> recetteFactory.createFromDTO(dto)
        );

        assertEquals("L'ID ou le nom de l'aliment est requis", exception.getMessage());
    }

    @Test
    @DisplayName("updateFromDTO - devrait mettre Ã  jour recette existante")
    void updateFromDTO_devraitMettreAJourRecetteExistante() {
        Recette existing = new Recette();
        existing.setId(1L);
        existing.setTitre("Ancienne recette");
        existing.setIngredients(new ArrayList<>());
        existing.setEtapes(new ArrayList<>());

        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(
            null, 1L, "Tomate", null, 300.0f, null, true
        );

        RecetteDTO dto = new RecetteDTO(
            null, "Nouvelle recette", "Nouvelle description", 20, 200, null,
            Recette.Difficulte.MOYEN, null, null, null, null, null, null, null,
            Arrays.asList(ingredientDTO), null
        );

        when(alimentRepository.findById(1L)).thenReturn(Optional.of(aliment));

        Recette updated = recetteFactory.updateFromDTO(existing, dto);

        assertNotNull(updated);
        assertEquals("Nouvelle recette", updated.getTitre());
        assertEquals("Nouvelle description", updated.getDescription());
        assertEquals(20, updated.getTempsTotal());
        assertEquals(200, updated.getKcal());
        assertEquals(1, updated.getIngredients().size());
        assertEquals(aliment, updated.getIngredients().get(0).getAliment());
        verify(alimentRepository, times(1)).findById(1L);
    }
}

