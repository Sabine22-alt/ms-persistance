package com.springbootTemplate.univ.soa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbootTemplate.univ.soa.dto.AlimentDTO;
import com.springbootTemplate.univ.soa.mapper.AlimentMapper;
import com.springbootTemplate.univ.soa.model.Aliment;
import com.springbootTemplate.univ.soa.service.AlimentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(AlimentController.class)
@ActiveProfiles("test")
@DisplayName("Tests unitaires pour AlimentController")
class AlimentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlimentService alimentService;

    @MockitoBean
    private AlimentMapper alimentMapper;

    private Aliment aliment;
    private AlimentDTO alimentDTO;

    @BeforeEach
    void setUp() {
        aliment = new Aliment();
        aliment.setId(1L);
        aliment.setNom("Tomate");
        aliment.setCategorieAliment(Aliment.CategorieAliment.LEGUME);

        alimentDTO = new AlimentDTO();
        alimentDTO.setId(1L);
        alimentDTO.setNom("Tomate");
        alimentDTO.setCategorieAliment(Aliment.CategorieAliment.LEGUME);
    }

    // ==================== Tests pour GET /api/persistance/aliments ====================

    @Test
    @DisplayName("GET /api/persistance/aliments - devrait retourner tous les aliments")
    void getAllAliments_devraitRetournerTousLesAliments() throws Exception {
        // Given
        Aliment aliment2 = new Aliment();
        aliment2.setId(2L);
        aliment2.setNom("Carotte");

        AlimentDTO dto2 = new AlimentDTO();
        dto2.setId(2L);
        dto2.setNom("Carotte");

        when(alimentService.findAll()).thenReturn(Arrays.asList(aliment, aliment2));
        when(alimentMapper.toDTO(aliment)).thenReturn(alimentDTO);
        when(alimentMapper.toDTO(aliment2)).thenReturn(dto2);

        // When & Then
        mockMvc.perform(get("/api/persistance/aliments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("Tomate"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nom").value("Carotte"));

        verify(alimentService, times(1)).findAll();
        verify(alimentMapper, times(2)).toDTO(any(Aliment.class));
    }

    // ==================== Tests pour GET /api/persistance/aliments/{id} ====================

    @Test
    @DisplayName("GET /api/persistance/aliments/{id} - avec ID existant, devrait retourner l'aliment")
    void getAlimentById_avecIdExistant_devraitRetournerAliment() throws Exception {
        // Given
        when(alimentService.findById(1L)).thenReturn(Optional.of(aliment));
        when(alimentMapper.toDTO(aliment)).thenReturn(alimentDTO);

        // When & Then
        mockMvc.perform(get("/api/persistance/aliments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Tomate"))
                .andExpect(jsonPath("$.categorieAliment").value("LEGUME"));

        verify(alimentService, times(1)).findById(1L);
        verify(alimentMapper, times(1)).toDTO(aliment);
    }

    @Test
    @DisplayName("GET /api/persistance/aliments/{id} - avec ID inexistant, devrait retourner 404")
    void getAlimentById_avecIdInexistant_devraitRetourner404() throws Exception {
        // Given
        when(alimentService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/persistance/aliments/999"))
                .andExpect(status().isNotFound());

        verify(alimentService, times(1)).findById(999L);
        verify(alimentMapper, never()).toDTO(any());
    }

    // ==================== Tests pour POST /api/persistance/aliments ====================

    @Test
    @DisplayName("POST /api/persistance/aliments - avec données valides, devrait créer l'aliment")
    void createAliment_avecDonneesValides_devraitCreerAliment() throws Exception {
        // Given
        AlimentDTO newDTO = new AlimentDTO();
        newDTO.setNom("Pomme");
        newDTO.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        Aliment newAliment = new Aliment();
        newAliment.setNom("Pomme");
        newAliment.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        Aliment savedAliment = new Aliment();
        savedAliment.setId(3L);
        savedAliment.setNom("Pomme");
        savedAliment.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        AlimentDTO savedDTO = new AlimentDTO();
        savedDTO.setId(3L);
        savedDTO.setNom("Pomme");
        savedDTO.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        when(alimentService.findByNom("Pomme")).thenReturn(Optional.empty());
        when(alimentMapper.toEntity(any(AlimentDTO.class))).thenReturn(newAliment);
        when(alimentService.save(any(Aliment.class))).thenReturn(savedAliment);
        when(alimentMapper.toDTO(savedAliment)).thenReturn(savedDTO);

        // When & Then
        mockMvc.perform(post("/api/persistance/aliments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nom").value("Pomme"))
                .andExpect(jsonPath("$.categorieAliment").value("FRUIT"));

        verify(alimentService, times(1)).findByNom("Pomme");
        verify(alimentService, times(1)).save(any(Aliment.class));
    }

    @Test
    @DisplayName("POST /api/persistance/aliments - sans nom, devrait retourner 400")
    void createAliment_sansNom_devraitRetourner400() throws Exception {
        // Given
        AlimentDTO dto = new AlimentDTO();
        dto.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        // When & Then
        mockMvc.perform(post("/api/persistance/aliments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le nom de l'aliment est obligatoire"));

        verify(alimentService, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/persistance/aliments - avec nom trop court, devrait retourner 400")
    void createAliment_avecNomTropCourt_devraitRetourner400() throws Exception {
        // Given
        AlimentDTO dto = new AlimentDTO();
        dto.setNom("A");
        dto.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        // When & Then
        mockMvc.perform(post("/api/persistance/aliments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le nom doit contenir au moins 2 caractères"));

        verify(alimentService, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/persistance/aliments - avec nom existant, devrait retourner 409")
    void createAliment_avecNomExistant_devraitRetourner409() throws Exception {
        // Given
        AlimentDTO dto = new AlimentDTO();
        dto.setNom("Tomate");
        dto.setCategorieAliment(Aliment.CategorieAliment.LEGUME);

        when(alimentService.findByNom("Tomate")).thenReturn(Optional.of(aliment));

        // When & Then
        mockMvc.perform(post("/api/persistance/aliments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Un aliment avec ce nom existe déjà"));

        verify(alimentService, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/persistance/aliments - sans catégorie, devrait retourner 400")
    void createAliment_sansCategorie_devraitRetourner400() throws Exception {
        // Given
        AlimentDTO dto = new AlimentDTO();
        dto.setNom("Pomme");

        when(alimentService.findByNom("Pomme")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/persistance/aliments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La catégorie est obligatoire"));

        verify(alimentService, never()).save(any());
    }

    // ==================== Tests pour PUT /api/persistance/aliments/{id} ====================

    @Test
    @DisplayName("PUT /api/persistance/aliments/{id} - avec données valides, devrait mettre à jour")
    void updateAliment_avecDonneesValides_devraitMettreAJour() throws Exception {
        // Given
        AlimentDTO updateDTO = new AlimentDTO();
        updateDTO.setNom("Tomate cerise");
        updateDTO.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        Aliment updatedAliment = new Aliment();
        updatedAliment.setId(1L);
        updatedAliment.setNom("Tomate cerise");
        updatedAliment.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        AlimentDTO updatedDTO = new AlimentDTO();
        updatedDTO.setId(1L);
        updatedDTO.setNom("Tomate cerise");
        updatedDTO.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        when(alimentService.findById(1L)).thenReturn(Optional.of(aliment));
        when(alimentService.findByNom("Tomate cerise")).thenReturn(Optional.empty());
        when(alimentMapper.toEntity(any(AlimentDTO.class))).thenReturn(updatedAliment);
        when(alimentService.update(eq(1L), any(Aliment.class))).thenReturn(updatedAliment);
        when(alimentMapper.toDTO(updatedAliment)).thenReturn(updatedDTO);

        // When & Then
        mockMvc.perform(put("/api/persistance/aliments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Tomate cerise"));

        verify(alimentService, times(1)).update(eq(1L), any(Aliment.class));
    }

    @Test
    @DisplayName("PUT /api/persistance/aliments/{id} - avec ID inexistant, devrait retourner 404")
    void updateAliment_avecIdInexistant_devraitRetourner404() throws Exception {
        // Given
        AlimentDTO dto = new AlimentDTO();
        dto.setNom("Test");
        dto.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        when(alimentService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/persistance/aliments/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Aliment non trouvé avec l'ID: 999"));

        verify(alimentService, never()).update(any(), any());
    }

    // ==================== Tests pour DELETE /api/persistance/aliments/{id} ====================

    @Test
    @DisplayName("DELETE /api/persistance/aliments/{id} - avec ID existant, devrait supprimer")
    void deleteAliment_avecIdExistant_devraitSupprimer() throws Exception {
        // Given
        when(alimentService.findById(1L)).thenReturn(Optional.of(aliment));
        doNothing().when(alimentService).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/api/persistance/aliments/1"))
                .andExpect(status().isNoContent());

        verify(alimentService, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("DELETE /api/persistance/aliments/{id} - avec ID inexistant, devrait retourner 404")
    void deleteAliment_avecIdInexistant_devraitRetourner404() throws Exception {
        // Given
        when(alimentService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/persistance/aliments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Aliment non trouvé avec l'ID: 999"));

        verify(alimentService, never()).deleteById(any());
    }
}