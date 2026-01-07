package com.mspersistance.univ.soa.mapper;

import com.mspersistance.univ.soa.dto.RecetteDTO;
import com.mspersistance.univ.soa.model.Aliment;
import com.mspersistance.univ.soa.model.Etape;
import com.mspersistance.univ.soa.model.Ingredient;
import com.mspersistance.univ.soa.model.Recette;
import com.mspersistance.univ.soa.service.MinioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour RecetteMapper")
class RecetteMapperTest {

    @Mock
    private MinioService minioService;

    private RecetteMapper recetteMapper;

    @BeforeEach
    void setUp() {
        recetteMapper = new RecetteMapper(minioService);
    }

    // ==================== Tests pour toDTO() ====================

    @Test
    @DisplayName("toDTO - avec entitÃ© complÃ¨te, devrait convertir en DTO")
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
        assertEquals(10L, dto.id());
        assertEquals("Salade", dto.titre());
        assertEquals(20, dto.tempsTotal());
        assertEquals(150, dto.kcal());
        assertEquals("http://example.com/image.jpg", dto.imageUrl());
        assertEquals(Recette.Difficulte.FACILE, dto.difficulte());
        assertEquals(now, dto.dateCreation());
        assertEquals(now, dto.dateModification());

        assertNotNull(dto.ingredients());
        assertEquals(1, dto.ingredients().size());
        RecetteDTO.IngredientDTO ingredientDTO = dto.ingredients().get(0);
        assertEquals(1L, ingredientDTO.id());
        assertEquals(1L, ingredientDTO.alimentId());
        assertEquals("Tomate", ingredientDTO.alimentNom());
        assertEquals(200.0f, ingredientDTO.quantite());
        assertEquals("GRAMME", ingredientDTO.unite());
        assertTrue(ingredientDTO.principal());

        assertNotNull(dto.etapes());
        assertEquals(1, dto.etapes().size());
        RecetteDTO.EtapeDTO etapeDTO = dto.etapes().get(0);
        assertEquals(1L, etapeDTO.id());
        assertEquals(1, etapeDTO.ordre());
        assertEquals(10, etapeDTO.temps());
        assertEquals("Couper les tomates", etapeDTO.texte());
    }

    @Test
    @DisplayName("toDTO - avec entitÃ© null, devrait retourner null")
    void toDTO_avecEntiteNull_devraitRetournerNull() {
        // When
        RecetteDTO dto = recetteMapper.toDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("toDTO - sans ingrÃ©dients, liste devrait Ãªtre null")
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
        assertNull(dto.ingredients());
    }

    @Test
    @DisplayName("toDTO - sans Ã©tapes, liste devrait Ãªtre null")
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
        assertNull(dto.etapes());
    }

    @Test
    @DisplayName("toDTO - ingrÃ©dient sans unitÃ©, unite devrait Ãªtre null")
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
        assertNotNull(dto.ingredients());
        assertNull(dto.ingredients().get(0).unite());
    }

    @Test
    @DisplayName("toDTO - ingrÃ©dient sans aliment rÃ©fÃ©rencÃ©, devrait utiliser nomAliment")
    void toDTO_ingredientSansAlimentReference_devraitUtiliserNomAliment() {
        // Given
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setAliment(null);  // Pas de rÃ©fÃ©rence aliment
        ingredient.setNomAliment("Sel de mer");  // Nom libre
        ingredient.setQuantite(5.0f);
        ingredient.setUnite(Ingredient.Unite.GRAMME);

        Recette recette = new Recette();
        recette.setId(10L);
        recette.setTitre("Test");
        recette.setIngredients(new ArrayList<>(Arrays.asList(ingredient)));

        // When
        RecetteDTO dto = recetteMapper.toDTO(recette);

        // Then
        assertNotNull(dto);
        assertNotNull(dto.ingredients());
        RecetteDTO.IngredientDTO ingredientDTO = dto.ingredients().get(0);
        assertNull(ingredientDTO.alimentId());
        assertEquals("Sel de mer", ingredientDTO.alimentNom());
        assertEquals("Sel de mer", ingredientDTO.nomAliment());
    }

    // ==================== Tests pour toEntity() ====================

    @Test
    @DisplayName("toEntity - avec DTO valide, devrait convertir en entitÃ©")
    void toEntity_avecDTOValide_devraitConvertirEnEntite() {
        RecetteDTO dto = new RecetteDTO(
            10L, "PÃ¢tes", null, 25, 300, "http://example.com/pates.jpg",
            Recette.Difficulte.MOYEN, null, null, null, null, null, null, null, null, null
        );

        Recette recette = recetteMapper.toEntity(dto);

        assertNotNull(recette);
        assertEquals(10L, recette.getId());
        assertEquals("PÃ¢tes", recette.getTitre());
        assertEquals(25, recette.getTempsTotal());
        assertEquals(300, recette.getKcal());
        assertEquals("http://example.com/pates.jpg", recette.getImageUrl());
        assertEquals(Recette.Difficulte.MOYEN, recette.getDifficulte());
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
        RecetteDTO dto = new RecetteDTO(
            null, "Nouvelle recette", null, 15, null, null, null, null, null, null, null, null, null, null, null, null
        );

        Recette recette = recetteMapper.toEntity(dto);

        assertNotNull(recette);
        assertNull(recette.getId());
        assertEquals("Nouvelle recette", recette.getTitre());
    }

    // ==================== Tests de conversion bidirectionnelle ====================

    @Test
    @DisplayName("conversion bidirectionnelle - devrait prÃ©server les donnÃ©es principales")
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
