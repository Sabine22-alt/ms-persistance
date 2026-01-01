package com.springbootTemplate.univ.soa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbootTemplate.univ.soa.config.TestSecurityConfig;
import com.springbootTemplate.univ.soa.dto.UtilisateurDTO;
import com.springbootTemplate.univ.soa.mapper.UtilisateurMapper;
import com.springbootTemplate.univ.soa.model.Utilisateur;
import com.springbootTemplate.univ.soa.service.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(UtilisateurController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("Tests unitaires pour UtilisateurController")
class UtilisateurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UtilisateurService utilisateurService;

    @MockitoBean
    private UtilisateurMapper utilisateurMapper;

    private Utilisateur utilisateur;
    private UtilisateurDTO utilisateurDTO;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setEmail("john@test.com");
        utilisateur.setNom("Doe");
        utilisateur.setPrenom("John");
        utilisateur.setActif(true);
        utilisateur.setRole(Utilisateur.Role.USER);
        utilisateur.setAlimentsExclus(new HashSet<>());

        utilisateurDTO = new UtilisateurDTO();
        utilisateurDTO.setId(1L);
        utilisateurDTO.setEmail("john@test.com");
        utilisateurDTO.setNom("Doe");
        utilisateurDTO.setPrenom("John");
        utilisateurDTO.setActif(true);
        utilisateurDTO.setRole(Utilisateur.Role.USER);
        utilisateurDTO.setAlimentsExclusIds(new HashSet<>());
    }

    // ==================== Tests pour GET /api/persistance/utilisateurs ====================

    @Test
    @DisplayName("GET /api/persistance/utilisateurs - devrait retourner tous les utilisateurs")
    void getAllUtilisateurs_devraitRetournerTousLesUtilisateurs() throws Exception {
        // Given
        Utilisateur utilisateur2 = new Utilisateur();
        utilisateur2.setId(2L);
        utilisateur2.setEmail("jane@test.com");
        utilisateur2.setNom("Smith");

        UtilisateurDTO dto2 = new UtilisateurDTO();
        dto2.setId(2L);
        dto2.setEmail("jane@test.com");
        dto2.setNom("Smith");

        when(utilisateurService.findAll()).thenReturn(Arrays.asList(utilisateur, utilisateur2));
        when(utilisateurMapper.toDTO(utilisateur)).thenReturn(utilisateurDTO);
        when(utilisateurMapper.toDTO(utilisateur2)).thenReturn(dto2);

        // When & Then
        mockMvc.perform(get("/api/persistance/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("john@test.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].email").value("jane@test.com"));

        verify(utilisateurService, times(1)).findAll();
        verify(utilisateurMapper, times(2)).toDTO(any(Utilisateur.class));
    }

    // ==================== Tests pour GET /api/persistance/utilisateurs/{id} ====================

    @Test
    @DisplayName("GET /api/persistance/utilisateurs/{id} - avec ID existant, devrait retourner l'utilisateur")
    void getUtilisateurById_avecIdExistant_devraitRetournerUtilisateur() throws Exception {
        // Given
        when(utilisateurService.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurMapper.toDTO(utilisateur)).thenReturn(utilisateurDTO);

        // When & Then
        mockMvc.perform(get("/api/persistance/utilisateurs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.nom").value("Doe"))
                .andExpect(jsonPath("$.prenom").value("John"));

        verify(utilisateurService, times(1)).findById(1L);
        verify(utilisateurMapper, times(1)).toDTO(utilisateur);
    }

    @Test
    @DisplayName("GET /api/persistance/utilisateurs/{id} - avec ID inexistant, devrait retourner 404")
    void getUtilisateurById_avecIdInexistant_devraitRetourner404() throws Exception {
        // Given
        when(utilisateurService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/persistance/utilisateurs/999"))
                .andExpect(status().isNotFound());

        verify(utilisateurService, times(1)).findById(999L);
        verify(utilisateurMapper, never()).toDTO(any());
    }

    // ==================== Tests pour GET /api/persistance/utilisateurs/email/{email} ====================

    @Test
    @DisplayName("GET /api/persistance/utilisateurs/email/{email} - avec email existant, devrait retourner l'utilisateur")
    void getUtilisateurByEmail_avecEmailExistant_devraitRetournerUtilisateur() throws Exception {
        // Given
        when(utilisateurService.findByEmail("john@test.com")).thenReturn(Optional.of(utilisateur));
        when(utilisateurMapper.toDTO(utilisateur)).thenReturn(utilisateurDTO);

        // When & Then
        mockMvc.perform(get("/api/persistance/utilisateurs/email/john@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@test.com"));

        verify(utilisateurService, times(1)).findByEmail("john@test.com");
    }

    @Test
    @DisplayName("GET /api/persistance/utilisateurs/email/{email} - avec email inexistant, devrait retourner 404")
    void getUtilisateurByEmail_avecEmailInexistant_devraitRetourner404() throws Exception {
        // Given
        when(utilisateurService.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/persistance/utilisateurs/email/unknown@test.com"))
                .andExpect(status().isNotFound());

        verify(utilisateurService, times(1)).findByEmail("unknown@test.com");
    }

    // ==================== Tests pour POST /api/persistance/utilisateurs ====================

    @Test
    @DisplayName("POST /api/persistance/utilisateurs - avec données valides, devrait créer l'utilisateur")
    void createUtilisateur_avecDonneesValides_devraitCreerUtilisateur() throws Exception {
        // Given
        UtilisateurDTO newDTO = new UtilisateurDTO();
        newDTO.setEmail("new@test.com");
        newDTO.setMotDePasse("password123");
        newDTO.setNom("New");
        newDTO.setPrenom("User");

        Utilisateur savedUtilisateur = new Utilisateur();
        savedUtilisateur.setId(3L);
        savedUtilisateur.setEmail("new@test.com");
        savedUtilisateur.setNom("New");
        savedUtilisateur.setPrenom("User");

        UtilisateurDTO savedDTO = new UtilisateurDTO();
        savedDTO.setId(3L);
        savedDTO.setEmail("new@test.com");
        savedDTO.setNom("New");
        savedDTO.setPrenom("User");

        when(utilisateurService.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(utilisateurService.saveFromDTO(any(UtilisateurDTO.class))).thenReturn(savedUtilisateur);
        when(utilisateurMapper.toDTO(savedUtilisateur)).thenReturn(savedDTO);

        // When & Then
        mockMvc.perform(post("/api/persistance/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("new@test.com"));

        verify(utilisateurService, times(1)).saveFromDTO(any(UtilisateurDTO.class));
    }

    @Test
    @DisplayName("POST /api/persistance/utilisateurs - sans email, devrait retourner 400")
    void createUtilisateur_sansEmail_devraitRetourner400() throws Exception {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setMotDePasse("password123");
        dto.setNom("Test");
        dto.setPrenom("User");

        // When & Then
        mockMvc.perform(post("/api/persistance/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("L'email est obligatoire"));

        verify(utilisateurService, never()).saveFromDTO(any());
    }

    @Test
    @DisplayName("POST /api/persistance/utilisateurs - avec email invalide, devrait retourner 400")
    void createUtilisateur_avecEmailInvalide_devraitRetourner400() throws Exception {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("email-invalide");
        dto.setMotDePasse("password123");
        dto.setNom("Test");
        dto.setPrenom("User");

        // When & Then
        mockMvc.perform(post("/api/persistance/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Format d'email invalide"));

        verify(utilisateurService, never()).saveFromDTO(any());
    }

    @Test
    @DisplayName("POST /api/persistance/utilisateurs - avec email existant, devrait retourner 409")
    void createUtilisateur_avecEmailExistant_devraitRetourner409() throws Exception {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("john@test.com");
        dto.setMotDePasse("password123");
        dto.setNom("Test");
        dto.setPrenom("User");

        when(utilisateurService.findByEmail("john@test.com")).thenReturn(Optional.of(utilisateur));

        // When & Then
        mockMvc.perform(post("/api/persistance/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Un utilisateur avec cet email existe déjà"));

        verify(utilisateurService, never()).saveFromDTO(any());
    }

    @Test
    @DisplayName("POST /api/persistance/utilisateurs - sans mot de passe, devrait retourner 400")
    void createUtilisateur_sansMotDePasse_devraitRetourner400() throws Exception {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("test@test.com");
        dto.setNom("Test");
        dto.setPrenom("User");

        when(utilisateurService.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/persistance/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le mot de passe est obligatoire"));

        verify(utilisateurService, never()).saveFromDTO(any());
    }

    @Test
    @DisplayName("POST /api/persistance/utilisateurs - avec mot de passe trop court, devrait retourner 400")
    void createUtilisateur_avecMotDePasseTropCourt_devraitRetourner400() throws Exception {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("test@test.com");
        dto.setMotDePasse("12345"); // < 6 caractères
        dto.setNom("Test");
        dto.setPrenom("User");

        when(utilisateurService.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/persistance/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le mot de passe doit contenir au moins 6 caractères"));

        verify(utilisateurService, never()).saveFromDTO(any());
    }

    @Test
    @DisplayName("POST /api/persistance/utilisateurs - sans nom, devrait retourner 400")
    void createUtilisateur_sansNom_devraitRetourner400() throws Exception {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("test@test.com");
        dto.setMotDePasse("password123");
        dto.setPrenom("User");

        when(utilisateurService.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/persistance/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le nom est obligatoire"));

        verify(utilisateurService, never()).saveFromDTO(any());
    }

    @Test
    @DisplayName("POST /api/persistance/utilisateurs - sans prénom, devrait retourner 400")
    void createUtilisateur_sansPrenom_devraitRetourner400() throws Exception {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("test@test.com");
        dto.setMotDePasse("password123");
        dto.setNom("Test");

        when(utilisateurService.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/persistance/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Le prénom est obligatoire"));

        verify(utilisateurService, never()).saveFromDTO(any());
    }

    // ==================== Tests pour PUT /api/persistance/utilisateurs/{id} ====================

    @Test
    @DisplayName("PUT /api/persistance/utilisateurs/{id} - avec données valides, devrait mettre à jour")
    void updateUtilisateur_avecDonneesValides_devraitMettreAJour() throws Exception {
        // Given
        UtilisateurDTO updateDTO = new UtilisateurDTO();
        updateDTO.setEmail("john.updated@test.com");
        updateDTO.setNom("Doe Updated");
        updateDTO.setPrenom("John Updated");

        Utilisateur updatedUtilisateur = new Utilisateur();
        updatedUtilisateur.setId(1L);
        updatedUtilisateur.setEmail("john.updated@test.com");
        updatedUtilisateur.setNom("Doe Updated");
        updatedUtilisateur.setPrenom("John Updated");

        UtilisateurDTO updatedDTO = new UtilisateurDTO();
        updatedDTO.setId(1L);
        updatedDTO.setEmail("john.updated@test.com");
        updatedDTO.setNom("Doe Updated");
        updatedDTO.setPrenom("John Updated");

        when(utilisateurService.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurService.findByEmail("john.updated@test.com")).thenReturn(Optional.empty());
        when(utilisateurService.updateFromDTO(eq(1L), any(UtilisateurDTO.class))).thenReturn(updatedUtilisateur);
        when(utilisateurMapper.toDTO(updatedUtilisateur)).thenReturn(updatedDTO);

        // When & Then
        mockMvc.perform(put("/api/persistance/utilisateurs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john.updated@test.com"));

        verify(utilisateurService, times(1)).updateFromDTO(eq(1L), any(UtilisateurDTO.class));
    }

    @Test
    @DisplayName("PUT /api/persistance/utilisateurs/{id} - avec ID inexistant, devrait retourner 404")
    void updateUtilisateur_avecIdInexistant_devraitRetourner404() throws Exception {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("test@test.com");
        dto.setNom("Test");
        dto.setPrenom("User");

        when(utilisateurService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/persistance/utilisateurs/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(utilisateurService, never()).updateFromDTO(any(), any());
    }

    // ==================== Tests pour DELETE /api/persistance/utilisateurs/{id} ====================

    @Test
    @DisplayName("DELETE /api/persistance/utilisateurs/{id} - avec ID existant, devrait supprimer")
    void deleteUtilisateur_avecIdExistant_devraitSupprimer() throws Exception {
        // Given
        when(utilisateurService.findById(1L)).thenReturn(Optional.of(utilisateur));
        doNothing().when(utilisateurService).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/api/persistance/utilisateurs/1"))
                .andExpect(status().isNoContent());

        verify(utilisateurService, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("DELETE /api/persistance/utilisateurs/{id} - avec ID inexistant, devrait retourner 404")
    void deleteUtilisateur_avecIdInexistant_devraitRetourner404() throws Exception {
        // Given
        when(utilisateurService.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/persistance/utilisateurs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Utilisateur non trouvé avec l'ID: 999"));

        verify(utilisateurService, never()).deleteById(any());
    }
}

