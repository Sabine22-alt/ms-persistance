package com.springbootTemplate.univ.soa.mapper;

import com.springbootTemplate.univ.soa.dto.RecetteDTO;
import com.springbootTemplate.univ.soa.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires pour RecetteMapper")
class RecetteMapperTest {

    private RecetteMapper recetteMapper;

    @BeforeEach
    void setUp() {
        recetteMapper = new RecetteMapper();
    }

    // ==================== Tests pour toDTO() ====================

    @Test
    @DisplayName("toDTO - avec entité complète, devrait convertir en DTO")
    void toDTO_avecEntiteComplete_devraitConvertirEnDTO() {
        // Given
        Aliment aliment = new Aliment();
        aliment.setId(1L);
        aliment.setNom("Tomate");

        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setAliment(aliment);
        ingredient.setQuantite(200.0f);
        ingredient.setUnite(Ingredient.Unite.GRAMME);
        ingredient.setPrincipal(true);

        Etape etape = new Etape();
        etape.setId(1L);
        etape.setOrdre(1);
        etape.setTemps(10);
        etape.setTexte("Couper les tomates");

        LocalDateTime now = LocalDateTime.now();

        Recette recette = new Recette();
        recette.setId(10L);
        recette.setTitre("Salade");
        recette.setTempsTotal(20);
        recette.setKcal(150);
        recette.setImageUrl("http://example.com/image.jpg");
        recette.setDifficulte(Recette.Difficulte.FACILE);
        recette.setDateCreation(now);
        recette.setDateModification(now);
        recette.setIngredients(new ArrayList<>(Arrays.asList(ingredient)));
        recette.setEtapes(new ArrayList<>(Arrays.asList(etape)));

        // When
        RecetteDTO dto = recetteMapper.toDTO(recette);

        // Then
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Salade", dto.getTitre());
        assertEquals(20, dto.getTempsTotal());
        assertEquals(150, dto.getKcal());
        assertEquals("http://example.com/image.jpg", dto.getImageUrl());
        assertEquals(Recette.Difficulte.FACILE, dto.getDifficulte());
        assertEquals(now, dto.getDateCreation());
        assertEquals(now, dto.getDateModification());

        // Vérifier les ingrédients
        assertNotNull(dto.getIngredients());
        assertEquals(1, dto.getIngredients().size());
        RecetteDTO.IngredientDTO ingredientDTO = dto.getIngredients().get(0);
        assertEquals(1L, ingredientDTO.getId());
        assertEquals(1L, ingredientDTO.getAlimentId());
        assertEquals("Tomate", ingredientDTO.getAlimentNom());
        assertEquals(200.0f, ingredientDTO.getQuantite());
        assertEquals("GRAMME", ingredientDTO.getUnite());
        assertTrue(ingredientDTO.getPrincipal());

        // Vérifier les étapes
        assertNotNull(dto.getEtapes());
        assertEquals(1, dto.getEtapes().size());
        RecetteDTO.EtapeDTO etapeDTO = dto.getEtapes().get(0);
        assertEquals(1L, etapeDTO.getId());
        assertEquals(1, etapeDTO.getOrdre());
        assertEquals(10, etapeDTO.getTemps());
        assertEquals("Couper les tomates", etapeDTO.getTexte());
    }

    @Test
    @DisplayName("toDTO - avec entité null, devrait retourner null")
    void toDTO_avecEntiteNull_devraitRetournerNull() {
        // When
        RecetteDTO dto = recetteMapper.toDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("toDTO - sans ingrédients, liste devrait être null")
    void toDTO_sansIngredients_listeDevraitEtreNull() {
        // Given
        Recette recette = new Recette();
        recette.setId(10L);
        recette.setTitre("Test");
        recette.setIngredients(null);

        // When
        RecetteDTO dto = recetteMapper.toDTO(recette);

        // Then
        assertNotNull(dto);
        assertNull(dto.getIngredients());
    }

    @Test
    @DisplayName("toDTO - sans étapes, liste devrait être null")
    void toDTO_sansEtapes_listeDevraitEtreNull() {
        // Given
        Recette recette = new Recette();
        recette.setId(10L);
        recette.setTitre("Test");
        recette.setEtapes(null);

        // When
        RecetteDTO dto = recetteMapper.toDTO(recette);

        // Then
        assertNotNull(dto);
        assertNull(dto.getEtapes());
    }

    @Test
    @DisplayName("toDTO - ingrédient sans unité, unite devrait être null")
    void toDTO_ingredientSansUnite_uniteDevraitEtreNull() {
        // Given
        Aliment aliment = new Aliment();
        aliment.setId(1L);
        aliment.setNom("Sel");

        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setAliment(aliment);
        ingredient.setQuantite(5.0f);
        ingredient.setUnite(null);

        Recette recette = new Recette();
        recette.setId(10L);
        recette.setTitre("Test");
        recette.setIngredients(new ArrayList<>(Arrays.asList(ingredient)));

        // When
        RecetteDTO dto = recetteMapper.toDTO(recette);

        // Then
        assertNotNull(dto);
        assertNotNull(dto.getIngredients());
        assertNull(dto.getIngredients().get(0).getUnite());
    }

    // ==================== Tests pour toEntity() ====================

    @Test
    @DisplayName("toEntity - avec DTO valide, devrait convertir en entité")
    void toEntity_avecDTOValide_devraitConvertirEnEntite() {
        // Given
        RecetteDTO dto = new RecetteDTO();
        dto.setId(10L);
        dto.setTitre("Pâtes");
        dto.setTempsTotal(25);
        dto.setKcal(300);
        dto.setImageUrl("http://example.com/pates.jpg");
        dto.setDifficulte(Recette.Difficulte.MOYEN);

        // When
        Recette recette = recetteMapper.toEntity(dto);

        // Then
        assertNotNull(recette);
        assertEquals(10L, recette.getId());
        assertEquals("Pâtes", recette.getTitre());
        assertEquals(25, recette.getTempsTotal());
        assertEquals(300, recette.getKcal());
        assertEquals("http://example.com/pates.jpg", recette.getImageUrl());
        assertEquals(Recette.Difficulte.MOYEN, recette.getDifficulte());
        // Note: ingrédients et étapes ne sont pas mappés dans toEntity
        assertNotNull(recette.getIngredients());
        assertTrue(recette.getIngredients().isEmpty());
        assertNotNull(recette.getEtapes());
        assertTrue(recette.getEtapes().isEmpty());
    }

    @Test
    @DisplayName("toEntity - avec DTO null, devrait retourner null")
    void toEntity_avecDTONull_devraitRetournerNull() {
        // When
        Recette recette = recetteMapper.toEntity(null);

        // Then
        assertNull(recette);
    }

    @Test
    @DisplayName("toEntity - avec ID null, devrait fonctionner")
    void toEntity_avecIdNull_devraitFonctionner() {
        // Given
        RecetteDTO dto = new RecetteDTO();
        dto.setTitre("Nouvelle recette");
        dto.setTempsTotal(15);

        // When
        Recette recette = recetteMapper.toEntity(dto);

        // Then
        assertNotNull(recette);
        assertNull(recette.getId());
        assertEquals("Nouvelle recette", recette.getTitre());
    }

    // ==================== Tests de conversion bidirectionnelle ====================

    @Test
    @DisplayName("conversion bidirectionnelle - devrait préserver les données principales")
    void conversionBidirectionnelle_devraitPreserverLesDonneesPrincipales() {
        // Given
        Recette original = new Recette();
        original.setId(20L);
        original.setTitre("Pizza");
        original.setTempsTotal(45);
        original.setKcal(400);
        original.setImageUrl("http://example.com/pizza.jpg");
        original.setDifficulte(Recette.Difficulte.DIFFICILE);

        // When
        RecetteDTO dto = recetteMapper.toDTO(original);
        Recette converti = recetteMapper.toEntity(dto);

        // Then
        assertNotNull(converti);
        assertEquals(original.getId(), converti.getId());
        assertEquals(original.getTitre(), converti.getTitre());
        assertEquals(original.getTempsTotal(), converti.getTempsTotal());
        assertEquals(original.getKcal(), converti.getKcal());
        assertEquals(original.getImageUrl(), converti.getImageUrl());
        assertEquals(original.getDifficulte(), converti.getDifficulte());
    }
}