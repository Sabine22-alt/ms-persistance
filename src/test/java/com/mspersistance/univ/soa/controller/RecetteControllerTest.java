package com.mspersistance.univ.soa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mspersistance.univ.soa.config.TestSecurityConfig;
import com.mspersistance.univ.soa.dto.RecetteDTO;
import com.mspersistance.univ.soa.mapper.RecetteMapper;
import com.mspersistance.univ.soa.model.Recette;
import com.mspersistance.univ.soa.service.RecetteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(RecetteController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("Tests unitaires pour RecetteController")
class RecetteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecetteService recetteService;

    @MockitoBean
    private RecetteMapper recetteMapper;

    private Recette recette;
    private RecetteDTO recetteDTO;

    @BeforeEach
    void setUp() {
        recette = new Recette();
        recette.setId(1L);
        recette.setTitre("Pâtes carbonara");
        recette.setTempsTotal(30);
        recette.setKcal(500);
        recette.setImageUrl("http://example.com/carbonara.jpg");
        recette.setDifficulte(Recette.Difficulte.MOYEN);
        recette.setIngredients(new ArrayList<>());
        recette.setEtapes(new ArrayList<>());

        recetteDTO = new RecetteDTO(
            1L, "Pâtes carbonara", null, 30, 500,
            "http://example.com/carbonara.jpg", Recette.Difficulte.MOYEN,
            null, null, null, null, null, null, null,
            new ArrayList<>(), new ArrayList<>()
        );
    }

    // ==================== Tests pour GET /api/persistance/recettes ====================

    @Test
    @DisplayName("GET /api/persistance/recettes - devrait retourner toutes les recettes")
    void getAllRecettes_devraitRetournerToutesLesRecettes() throws Exception {
        // Given
        Recette recette2 = new Recette();
        recette2.setId(2L);
        recette2.setTitre("Salade");
        recette2.setIngredients(new ArrayList<>());
        recette2.setEtapes(new ArrayList<>());

        RecetteDTO dto2 = new RecetteDTO(2L, "Salade", null, null, null, null, null, null, null, null, null, null, null, null, new ArrayList<>(), new ArrayList<>());

        when(recetteService.findAll()).thenReturn(Arrays.asList(recette, recette2));
        when(recetteMapper.toDTO(recette)).thenReturn(recetteDTO);
        when(recetteMapper.toDTO(recette2)).thenReturn(dto2);

        // When & Then
        mockMvc.perform(get("/api/persistance/recettes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titre").value("Pâtes carbonara"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].titre").value("Salade"));

        verify(recetteService, times(1)).findAll();
        verify(recetteMapper, times(2)).toDTO(any(Recette.class));
    }

    // ==================== Tests pour GET /api/persistance/recettes/{id} ====================

    @Test
    @DisplayName("GET /api/persistance/recettes/{id} - avec ID existant, devrait retourner la recette")
    void getRecetteById_avecIdExistant_devraitRetournerRecette() throws Exception {
        // Given
        when(recetteService.findById(1L)).thenReturn(Optional.of(recette));
        when(recetteMapper.toDTO(recette)).thenReturn(recetteDTO);

        // When & Then
        mockMvc.perform(get("/api/persistance/recettes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titre").value("Pâtes carbonara"))
                .andExpect(jsonPath("$.tempsTotal").value(30))
                .andExpect(jsonPath("$.kcal").value(500))
                .andExpect(jsonPath("$.difficulte").value("MOYEN"));

        verify(recetteService, times(1)).findById(1L);
        verify(recetteMapper, times(1)).toDTO(recette);
    }

    @Test
    @DisplayName("GET /api/persistance/recettes/{id} - avec ID inexistant, devrait retourner 404")
    void getRecetteById_avecIdInexistant_devraitRetourner404() throws Exception {
        // Given
        when(recetteService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/persistance/recettes/999"))
                .andExpect(status().isNotFound());

        verify(recetteService, times(1)).findById(999L);
        verify(recetteMapper, never()).toDTO(any());
    }

    // ==================== Tests pour POST /api/persistance/recettes ====================

    @Test
    @DisplayName("POST /api/persistance/recettes - avec données valides, devrait créer la recette")
    void createRecette_avecDonneesValides_devraitCreerRecette() throws Exception {
        // Given
        RecetteDTO newDTO = new RecetteDTO(null, "Pizza", null, 45, 800, null, Recette.Difficulte.MOYEN, null, null, null, null, null, null, null, new ArrayList<>(), new ArrayList<>());

        Recette savedRecette = new Recette();
        savedRecette.setId(3L);
        savedRecette.setTitre("Pizza");
        savedRecette.setTempsTotal(45);
        savedRecette.setKcal(800);
        savedRecette.setDifficulte(Recette.Difficulte.MOYEN);
        savedRecette.setIngredients(new ArrayList<>());
        savedRecette.setEtapes(new ArrayList<>());

        RecetteDTO savedDTO = new RecetteDTO(3L, "Pizza", null, 45, 800, null, Recette.Difficulte.MOYEN, null, null, null, null, null, null, null, new ArrayList<>(), new ArrayList<>());

        when(recetteService.saveFromDTO(any(RecetteDTO.class))).thenReturn(savedRecette);
        when(recetteMapper.toDTO(savedRecette)).thenReturn(savedDTO);

        // When & Then
        mockMvc.perform(post("/api/persistance/recettes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.titre").value("Pizza"))
                .andExpect(jsonPath("$.tempsTotal").value(45));

        verify(recetteService, times(1)).saveFromDTO(any(RecetteDTO.class));
    }

    @Test
    @DisplayName("POST /api/persistance/recettes - sans titre, devrait retourner 400")
    void createRecette_sansTitre_devraitRetourner400() throws Exception {
        // Given
        RecetteDTO dto = new RecetteDTO(null, null, null, 30, 500, null, null, null, null, null, null, null, null, null, null, null);

        // When & Then
        mockMvc.perform(post("/api/persistance/recettes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le titre de la recette est obligatoire"));

        verify(recetteService, never()).saveFromDTO(any());
    }

    @Test
    @DisplayName("POST /api/persistance/recettes - avec titre trop court, devrait retourner 400")
    void createRecette_avecTitreTropCourt_devraitRetourner400() throws Exception {
        // Given
        RecetteDTO dto = new RecetteDTO(null, "AB", null, 30, null, null, null, null, null, null, null, null, null, null, null, null);

        // When & Then
        mockMvc.perform(post("/api/persistance/recettes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le titre doit contenir au moins 3 caractères"));

        verify(recetteService, never()).saveFromDTO(any());
    }

    @Test
    @DisplayName("POST /api/persistance/recettes - avec temps total négatif, devrait retourner 400")
    void createRecette_avecTempsNegatif_devraitRetourner400() throws Exception {
        // Given
        RecetteDTO dto = new RecetteDTO(null, "Test", null, -10, null, null, null, null, null, null, null, null, null, null, null, null);

        // When & Then
        mockMvc.perform(post("/api/persistance/recettes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le temps total doit être supérieur à 0"));

        verify(recetteService, never()).saveFromDTO(any());
    }

    @Test
    @DisplayName("POST /api/persistance/recettes - avec kcal négatif, devrait retourner 400")
    void createRecette_avecKcalNegatif_devraitRetourner400() throws Exception {
        // Given
        RecetteDTO dto = new RecetteDTO(null, "Test", null, 30, -100, null, null, null, null, null, null, null, null, null, null, null);

        // When & Then
        mockMvc.perform(post("/api/persistance/recettes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Les calories ne peuvent pas être négatives"));

        verify(recetteService, never()).saveFromDTO(any());
    }

    @Test
    @DisplayName("POST /api/persistance/recettes - avec ingrédient sans alimentId ni alimentNom, devrait retourner 400")
    void createRecette_avecIngredientSansAlimentIdNiNom_devraitRetourner400() throws Exception {
        // Given
        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(null, null, null, null, 100.0f, null, null);

        RecetteDTO dto = new RecetteDTO(null, "Test", null, 30, null, null, null, null, null, null, null, null, null, null, new ArrayList<>(Arrays.asList(ingredientDTO)), null);

        // When & Then
        mockMvc.perform(post("/api/persistance/recettes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("L'ID ou le nom de l'aliment est requis pour chaque ingrédient"));

        verify(recetteService, never()).saveFromDTO(any());
    }

    @Test
    @DisplayName("POST /api/persistance/recettes - avec ingrédient alimentNom uniquement, devrait réussir")
    void createRecette_avecIngredientAlimentNom_devraitReussir() throws Exception {
        // Given
        RecetteDTO.IngredientDTO ingredientDTO = new RecetteDTO.IngredientDTO(null, null, "Tomate", "Tomate", 200.0f, "GRAMME", null);

        RecetteDTO newDTO = new RecetteDTO(null, "Nouvelle Recette", null, 30, null, null, null, null, null, null, null, null, null, null, new ArrayList<>(Arrays.asList(ingredientDTO)), null);

        Recette savedRecette = new Recette();
        savedRecette.setId(10L);
        savedRecette.setTitre("Nouvelle Recette");
        savedRecette.setTempsTotal(30);
        savedRecette.setIngredients(new ArrayList<>());
        savedRecette.setEtapes(new ArrayList<>());

        RecetteDTO.IngredientDTO savedIngredientDTO = new RecetteDTO.IngredientDTO(null, null, "Tomate", "Tomate", 200.0f, "GRAMME", null);
        RecetteDTO savedDTO = new RecetteDTO(10L, "Nouvelle Recette", null, 30, null, null, null, null, null, null, null, null, null, null, new ArrayList<>(Arrays.asList(savedIngredientDTO)), null);

        when(recetteService.saveFromDTO(any(RecetteDTO.class))).thenReturn(savedRecette);
        when(recetteMapper.toDTO(savedRecette)).thenReturn(savedDTO);

        // When & Then
        mockMvc.perform(post("/api/persistance/recettes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.titre").value("Nouvelle Recette"));

        verify(recetteService, times(1)).saveFromDTO(any(RecetteDTO.class));
    }

    @Test
    @DisplayName("POST /api/persistance/recettes - avec étape sans texte, devrait retourner 400")
    void createRecette_avecEtapeSansTexte_devraitRetourner400() throws Exception {
        // Given
        RecetteDTO.EtapeDTO etapeDTO = new RecetteDTO.EtapeDTO(null, 1, null, null);
        RecetteDTO dto = new RecetteDTO(null, "Test", null, 30, null, null, null, null, null, null, null, null, null, null, null, new ArrayList<>(Arrays.asList(etapeDTO)));

        // When & Then
        mockMvc.perform(post("/api/persistance/recettes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le texte de chaque étape est obligatoire"));

        verify(recetteService, never()).saveFromDTO(any());
    }

    // ==================== Tests pour PUT /api/persistance/recettes/{id} ====================

    @Test
    @DisplayName("PUT /api/persistance/recettes/{id} - avec données valides, devrait mettre à jour")
    void updateRecette_avecDonneesValides_devraitMettreAJour() throws Exception {
        // Given
        RecetteDTO updateDTO = new RecetteDTO(null, "Pâtes bolognaise", null, 40, 600, null, Recette.Difficulte.FACILE, null, null, null, null, null, null, null, new ArrayList<>(), new ArrayList<>());

        Recette updatedRecette = new Recette();
        updatedRecette.setId(1L);
        updatedRecette.setTitre("Pâtes bolognaise");
        updatedRecette.setTempsTotal(40);
        updatedRecette.setKcal(600);
        updatedRecette.setIngredients(new ArrayList<>());
        updatedRecette.setEtapes(new ArrayList<>());

        RecetteDTO updatedDTO = new RecetteDTO(1L, "Pâtes bolognaise", null, 40, 600, null, null, null, null, null, null, null, null, null, new ArrayList<>(), new ArrayList<>());

        when(recetteService.findById(1L)).thenReturn(Optional.of(recette));
        when(recetteService.updateFromDTO(eq(1L), any(RecetteDTO.class))).thenReturn(updatedRecette);
        when(recetteMapper.toDTO(updatedRecette)).thenReturn(updatedDTO);

        // When & Then
        mockMvc.perform(put("/api/persistance/recettes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titre").value("Pâtes bolognaise"));

        verify(recetteService, times(1)).updateFromDTO(eq(1L), any(RecetteDTO.class));
    }

    @Test
    @DisplayName("PUT /api/persistance/recettes/{id} - avec ID inexistant, devrait retourner 404")
    void updateRecette_avecIdInexistant_devraitRetourner404() throws Exception {
        // Given
        RecetteDTO dto = new RecetteDTO(null, "Test", null, 30, null, null, null, null, null, null, null, null, null, null, null, null);

        when(recetteService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/persistance/recettes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recette non trouvée avec l'ID: 999"));

        verify(recetteService, never()).updateFromDTO(any(), any());
    }

    // ==================== Tests pour DELETE /api/persistance/recettes/{id} ====================

    @Test
    @DisplayName("DELETE /api/persistance/recettes/{id} - avec ID existant, devrait supprimer")
    void deleteRecette_avecIdExistant_devraitSupprimer() throws Exception {
        // Given
        when(recetteService.findById(1L)).thenReturn(Optional.of(recette));
        doNothing().when(recetteService).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/api/persistance/recettes/1"))
                .andExpect(status().isNoContent());

        verify(recetteService, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("DELETE /api/persistance/recettes/{id} - avec ID inexistant, devrait retourner 404")
    void deleteRecette_avecIdInexistant_devraitRetourner404() throws Exception {
        // Given
        when(recetteService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/persistance/recettes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recette non trouvée avec l'ID: 999"));

        verify(recetteService, never()).deleteById(any());
    }
}

