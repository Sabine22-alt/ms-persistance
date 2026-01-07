package com.mspersistance.univ.soa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mspersistance.univ.soa.config.TestSecurityConfig;
import com.mspersistance.univ.soa.dto.UtilisateurDTO;
import com.mspersistance.univ.soa.mapper.UtilisateurMapper;
import com.mspersistance.univ.soa.model.Utilisateur;
import com.mspersistance.univ.soa.service.UtilisateurService;
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
import java.util.Set;

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
        utilisateur = Utilisateur.builder()
                .id(1L)
                .email("john@test.com")
                .nom("Doe")
                .prenom("John")
                .actif(true)
                .role(Utilisateur.Role.USER)
                .regimesAlimentaires(new HashSet<>())
                .allergenes(new HashSet<>())
                .typesCuisinePreferences(new HashSet<>())
                .alimentsExclus(new HashSet<>())
                .build();

        utilisateurDTO = UtilisateurDTO.builder()
                .id(1L)
                .email("john@test.com")
                .nom("Doe")
                .prenom("John")
                .actif(true)
                .role(Utilisateur.Role.USER)
                .regimesIds(new HashSet<>())
                .allergenesIds(new HashSet<>())
                .typesCuisinePreferesIds(new HashSet<>())
                .alimentsExclusIds(new HashSet<>())
                .build();
    }

    // ==================== Tests pour GET /api/persistance/utilisateurs ====================

    @Test
    @DisplayName("GET /api/persistance/utilisateurs - devrait retourner tous les utilisateurs")
    void getAllUtilisateurs_devraitRetournerTousLesUtilisateurs() throws Exception {
        // Given
        Utilisateur utilisateur2 = Utilisateur.builder()
                .id(2L)
                .email("jane@test.com")
                .nom("Smith")
                .build();

        UtilisateurDTO dto2 = UtilisateurDTO.builder()
                .id(2L)
                .email("jane@test.com")
                .nom("Smith")
                .build();
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
        UtilisateurDTO newDTO = UtilisateurDTO.builder()
                .email("new@test.com")
                .motDePasse("password123")
                .nom("New")
                .prenom("User")
                .build();

        Utilisateur savedUtilisateur = Utilisateur.builder()
                .id(3L)
                .email("new@test.com")
                .nom("New")
                .prenom("User")
                .build();

        UtilisateurDTO savedDTO = UtilisateurDTO.builder()
                .id(3L)
                .email("new@test.com")
                .nom("New")
                .prenom("User")
                .build();

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
    @DisplayName("POST /api/persistance/utilisateurs - avec préférences alimentaires, devrait créer l'utilisateur")
    void createUtilisateur_avecPreferencesAlimentaires_devraitCreerUtilisateur() throws Exception {
        // Given
        UtilisateurDTO newDTO = UtilisateurDTO.builder()
                .email("new@test.com")
                .motDePasse("password123")
                .nom("New")
                .prenom("User")
                .regimesIds(Set.of(1L))
                .allergenesIds(Set.of(8L))
                .typesCuisinePreferesIds(Set.of(2L, 5L))
                .build();

        Utilisateur savedUtilisateur = Utilisateur.builder()
                .id(3L)
                .email("new@test.com")
                .nom("New")
                .prenom("User")
                .build();

        UtilisateurDTO savedDTO = UtilisateurDTO.builder()
                .id(3L)
                .email("new@test.com")
                .nom("New")
                .prenom("User")
                .regimesIds(Set.of(1L))
                .allergenesIds(Set.of(8L))
                .typesCuisinePreferesIds(Set.of(2L, 5L))
                .build();

        when(utilisateurService.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(utilisateurService.saveFromDTO(any(UtilisateurDTO.class))).thenReturn(savedUtilisateur);
        when(utilisateurMapper.toDTO(savedUtilisateur)).thenReturn(savedDTO);

        // When & Then
        mockMvc.perform(post("/api/persistance/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.regimesIds[0]").value(1))
                .andExpect(jsonPath("$.allergenesIds[0]").value(8));

        verify(utilisateurService, times(1)).saveFromDTO(any(UtilisateurDTO.class));
    }

    @Test
    @DisplayName("POST /api/persistance/utilisateurs - sans email, devrait retourner 400")
    void createUtilisateur_sansEmail_devraitRetourner400() throws Exception {
        // Given
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .motDePasse("password123")
                .nom("Test")
                .prenom("User")
                .build();
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
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("email-invalide")
                .motDePasse("password123")
                .nom("Test")
                .prenom("User")
                .build();
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
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("john@test.com")
                .motDePasse("password123")
                .nom("Test")
                .prenom("User")
                .build();
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
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("test@test.com")
                .nom("Test")
                .prenom("User")
                .build();
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
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("test@test.com")
                .motDePasse("12345")
                .nom("Test")
                .prenom("User")
                .build();
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
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("test@test.com")
                .motDePasse("password123")
                .prenom("User")
                .build();
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
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("test@test.com")
                .motDePasse("password123")
                .nom("Test")
                .build();
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
        UtilisateurDTO updateDTO = UtilisateurDTO.builder()
                .email("john.updated@test.com")
                .nom("Doe Updated")
                .prenom("John Updated")
                .build();

        Utilisateur updatedUtilisateur = Utilisateur.builder()
                .id(1L)
                .email("john.updated@test.com")
                .nom("Doe Updated")
                .prenom("John Updated")
                .build();

        UtilisateurDTO updatedDTO = UtilisateurDTO.builder()
                .id(1L)
                .email("john.updated@test.com")
                .nom("Doe Updated")
                .prenom("John Updated")
                .build();

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
    @DisplayName("PUT /api/persistance/utilisateurs/{id} - avec préférences alimentaires, devrait mettre à jour")
    void updateUtilisateur_avecPreferencesAlimentaires_devraitMettreAJour() throws Exception {
        // Given
        UtilisateurDTO updateDTO = UtilisateurDTO.builder()
                .email("john@test.com")
                .nom("Doe")
                .prenom("John")
                .regimesIds(Set.of(1L, 2L))
                .allergenesIds(Set.of(7L, 8L))
                .typesCuisinePreferesIds(Set.of(2L, 3L, 5L))
                .build();

        Utilisateur updatedUtilisateur = Utilisateur.builder()
                .id(1L)
                .email("john@test.com")
                .build();

        UtilisateurDTO updatedDTO = UtilisateurDTO.builder()
                .id(1L)
                .email("john@test.com")
                .regimesIds(Set.of(1L, 2L))
                .allergenesIds(Set.of(7L, 8L))
                .typesCuisinePreferesIds(Set.of(2L, 3L, 5L))
                .build();

        when(utilisateurService.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurService.findByEmail("john@test.com")).thenReturn(Optional.of(utilisateur));
        when(utilisateurService.updateFromDTO(eq(1L), any(UtilisateurDTO.class))).thenReturn(updatedUtilisateur);
        when(utilisateurMapper.toDTO(updatedUtilisateur)).thenReturn(updatedDTO);

        // When & Then
        mockMvc.perform(put("/api/persistance/utilisateurs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(utilisateurService, times(1)).updateFromDTO(eq(1L), any(UtilisateurDTO.class));
    }

    @Test
    @DisplayName("PUT /api/persistance/utilisateurs/{id} - avec ID inexistant, devrait retourner 404")
    void updateUtilisateur_avecIdInexistant_devraitRetourner404() throws Exception {
        // Given
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("test@test.com")
                .nom("Test")
                .prenom("User")
                .build();
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
        doNothing().when(utilisateurService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/persistance/utilisateurs/1"))
                .andExpect(status().isNoContent());

        verify(utilisateurService, times(1)).delete(1L);
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

        verify(utilisateurService, never()).delete(any());
    }
}
