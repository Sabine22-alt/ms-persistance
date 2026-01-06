package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.dto.UtilisateurDTO;
import com.springbootTemplate.univ.soa.exception.ResourceNotFoundException;
import com.springbootTemplate.univ.soa.model.*;
import com.springbootTemplate.univ.soa.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour UtilisateurService")
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegimeAlimentaireRepository regimeAlimentaireRepository;

    @Mock
    private AllergeneRepository allergeneRepository;

    @Mock
    private TypeCuisineRepository typeCuisineRepository;

    @InjectMocks
    private UtilisateurService utilisateurService;

    private Utilisateur utilisateur;
    private RegimeAlimentaire regimeVegetarien;
    private Allergene allergeneFruitsACoque;
    private TypeCuisine cuisineItalienne;

    @BeforeEach
    void setUp() {
        // Régime alimentaire
        regimeVegetarien = new RegimeAlimentaire();
        regimeVegetarien.setId(1L);
        regimeVegetarien.setNom("Végétarien");

        // Allergène
        allergeneFruitsACoque = new Allergene();
        allergeneFruitsACoque.setId(8L);
        allergeneFruitsACoque.setNom("Fruits à coque");

        // Type de cuisine
        cuisineItalienne = new TypeCuisine();
        cuisineItalienne.setId(2L);
        cuisineItalienne.setNom("Italienne");

        // Utilisateur
        utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setEmail("john@test.com");
        utilisateur.setNom("Doe");
        utilisateur.setPrenom("John");
        utilisateur.setMotDePasse("hashedPassword123");
        utilisateur.setActif(true);
        utilisateur.setRole(Utilisateur.Role.USER);
        utilisateur.setRegimes(new HashSet<>());
        utilisateur.setAllergenes(new HashSet<>());
        utilisateur.setTypesCuisinePreferes(new HashSet<>());
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("findAll - devrait retourner tous les utilisateurs")
    void findAll_devraitRetournerTousLesUtilisateurs() {
        // Given
        Utilisateur utilisateur2 = new Utilisateur();
        utilisateur2.setId(2L);
        utilisateur2.setEmail("jane@test.com");
        utilisateur2.setNom("Smith");

        List<Utilisateur> utilisateurs = Arrays.asList(utilisateur, utilisateur2);
        when(utilisateurRepository.findAll()).thenReturn(utilisateurs);

        // When
        List<Utilisateur> result = utilisateurService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("john@test.com", result.get(0).getEmail());
        assertEquals("jane@test.com", result.get(1).getEmail());
        verify(utilisateurRepository, times(1)).findAll();
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("findById - avec ID existant, devrait retourner l'utilisateur")
    void findById_avecIdExistant_devraitRetournerUtilisateur() {
        // Given
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // When
        Optional<Utilisateur> result = utilisateurService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("john@test.com", result.get().getEmail());
        assertEquals("Doe", result.get().getNom());
        verify(utilisateurRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById - avec ID inexistant, devrait retourner Optional vide")
    void findById_avecIdInexistant_devraitRetournerOptionalVide() {
        // Given
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Utilisateur> result = utilisateurService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(utilisateurRepository, times(1)).findById(999L);
    }

    // ==================== Tests pour findByEmail() ====================

    @Test
    @DisplayName("findByEmail - avec email existant, devrait retourner l'utilisateur")
    void findByEmail_avecEmailExistant_devraitRetournerUtilisateur() {
        // Given
        when(utilisateurRepository.findByEmail("john@test.com")).thenReturn(Optional.of(utilisateur));

        // When
        Optional<Utilisateur> result = utilisateurService.findByEmail("john@test.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("john@test.com", result.get().getEmail());
        verify(utilisateurRepository, times(1)).findByEmail("john@test.com");
    }

    // ==================== Tests pour save() ====================

    @Test
    @DisplayName("save - avec utilisateur valide, devrait hasher le mot de passe et créer l'utilisateur")
    void save_avecUtilisateurValide_devraitHasherMotDePasseEtCreerUtilisateur() {
        // Given
        Utilisateur nouvelUtilisateur = new Utilisateur();
        nouvelUtilisateur.setEmail("new@test.com");
        nouvelUtilisateur.setNom("New");
        nouvelUtilisateur.setPrenom("User");
        nouvelUtilisateur.setMotDePasse("plainPassword");

        Utilisateur utilisateurSauvegarde = new Utilisateur();
        utilisateurSauvegarde.setId(2L);
        utilisateurSauvegarde.setEmail("new@test.com");
        utilisateurSauvegarde.setMotDePasse("hashedPassword");
        utilisateurSauvegarde.setActif(true);
        utilisateurSauvegarde.setRole(Utilisateur.Role.USER);

        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurSauvegarde);

        // When
        Utilisateur result = utilisateurService.save(nouvelUtilisateur);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("hashedPassword", result.getMotDePasse());
        assertTrue(result.getActif());
        assertEquals(Utilisateur.Role.USER, result.getRole());
        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("save - devrait définir les valeurs par défaut si nulles")
    void save_devraitDefinirValeursParDefautSiNulles() {
        // Given
        Utilisateur nouvelUtilisateur = new Utilisateur();
        nouvelUtilisateur.setEmail("test@test.com");
        // Actif et Role sont null

        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // When
        utilisateurService.save(nouvelUtilisateur);

        // Then
        assertTrue(nouvelUtilisateur.getActif());
        assertEquals(Utilisateur.Role.USER, nouvelUtilisateur.getRole());
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("save - devrait mettre l'ID à null")
    void save_devraitMettreIdANull() {
        // Given
        Utilisateur utilisateurAvecId = new Utilisateur();
        utilisateurAvecId.setId(999L);
        utilisateurAvecId.setEmail("test@test.com");

        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // When
        utilisateurService.save(utilisateurAvecId);

        // Then
        assertNull(utilisateurAvecId.getId());
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    // ==================== Tests pour deleteById() ====================

    @Test
    @DisplayName("deleteById - avec ID existant, devrait supprimer l'utilisateur")
    void deleteById_avecIdExistant_devraitSupprimerUtilisateur() {
        // Given
        when(utilisateurRepository.existsById(1L)).thenReturn(true);
        doNothing().when(utilisateurRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> utilisateurService.deleteById(1L));

        // Then
        verify(utilisateurRepository, times(1)).existsById(1L);
        verify(utilisateurRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById - avec ID inexistant, devrait lancer ResourceNotFoundException")
    void deleteById_avecIdInexistant_devraitLancerException() {
        // Given
        when(utilisateurRepository.existsById(999L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.deleteById(999L)
        );

        assertEquals("Utilisateur non trouvé avec l'ID: 999", exception.getMessage());
        verify(utilisateurRepository, times(1)).existsById(999L);
        verify(utilisateurRepository, never()).deleteById(any());
    }

    // ==================== Tests pour saveFromDTO() ====================

    @Test
    @DisplayName("saveFromDTO - avec préférences alimentaires, devrait créer l'utilisateur")
    void saveFromDTO_avecPreferencesAlimentaires_devraitCreerUtilisateur() {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("new@test.com");
        dto.setNom("New");
        dto.setPrenom("User");
        dto.setMotDePasse("plainPassword");
        dto.setRegimesIds(Set.of(1L));
        dto.setAllergenesIds(Set.of(8L));
        dto.setTypesCuisinePreferesIds(Set.of(2L));

        Utilisateur utilisateurSauvegarde = new Utilisateur();
        utilisateurSauvegarde.setId(2L);
        utilisateurSauvegarde.setEmail("new@test.com");

        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(regimeAlimentaireRepository.findById(1L)).thenReturn(Optional.of(regimeVegetarien));
        when(allergeneRepository.findById(8L)).thenReturn(Optional.of(allergeneFruitsACoque));
        when(typeCuisineRepository.findById(2L)).thenReturn(Optional.of(cuisineItalienne));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurSauvegarde);

        // When
        Utilisateur result = utilisateurService.saveFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(regimeAlimentaireRepository, times(1)).findById(1L);
        verify(allergeneRepository, times(1)).findById(8L);
        verify(typeCuisineRepository, times(1)).findById(2L);
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("saveFromDTO - avec régime inexistant, devrait lancer ResourceNotFoundException")
    void saveFromDTO_avecRegimeInexistant_devraitLancerException() {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("new@test.com");
        dto.setNom("New");
        dto.setPrenom("User");
        dto.setMotDePasse("plainPassword");
        dto.setRegimesIds(Set.of(999L));

        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(regimeAlimentaireRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.saveFromDTO(dto)
        );

        assertEquals("Régime alimentaire non trouvé avec l'ID: 999", exception.getMessage());
        verify(regimeAlimentaireRepository, times(1)).findById(999L);
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveFromDTO - avec allergène inexistant, devrait lancer ResourceNotFoundException")
    void saveFromDTO_avecAllergeneInexistant_devraitLancerException() {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("new@test.com");
        dto.setNom("New");
        dto.setPrenom("User");
        dto.setMotDePasse("plainPassword");
        dto.setAllergenesIds(Set.of(999L));

        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(allergeneRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.saveFromDTO(dto)
        );

        assertEquals("Allergène non trouvé avec l'ID: 999", exception.getMessage());
        verify(allergeneRepository, times(1)).findById(999L);
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveFromDTO - avec type de cuisine inexistant, devrait lancer ResourceNotFoundException")
    void saveFromDTO_avecTypeCuisineInexistant_devraitLancerException() {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("new@test.com");
        dto.setNom("New");
        dto.setPrenom("User");
        dto.setMotDePasse("plainPassword");
        dto.setTypesCuisinePreferesIds(Set.of(999L));

        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(typeCuisineRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.saveFromDTO(dto)
        );

        assertEquals("Type de cuisine non trouvé avec l'ID: 999", exception.getMessage());
        verify(typeCuisineRepository, times(1)).findById(999L);
        verify(utilisateurRepository, never()).save(any());
    }

    // ==================== Tests pour updateFromDTO() ====================

    @Test
    @DisplayName("updateFromDTO - avec préférences alimentaires, devrait mettre à jour l'utilisateur")
    void updateFromDTO_avecPreferencesAlimentaires_devraitMettreAJourUtilisateur() {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("john@test.com");
        dto.setNom("Doe Updated");
        dto.setPrenom("John Updated");
        dto.setRegimesIds(Set.of(1L));
        dto.setAllergenesIds(Set.of(8L));
        dto.setTypesCuisinePreferesIds(Set.of(2L));

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(regimeAlimentaireRepository.findById(1L)).thenReturn(Optional.of(regimeVegetarien));
        when(allergeneRepository.findById(8L)).thenReturn(Optional.of(allergeneFruitsACoque));
        when(typeCuisineRepository.findById(2L)).thenReturn(Optional.of(cuisineItalienne));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // When
        Utilisateur result = utilisateurService.updateFromDTO(1L, dto);

        // Then
        assertNotNull(result);
        assertEquals("Doe Updated", result.getNom());
        assertEquals("John Updated", result.getPrenom());
        verify(utilisateurRepository, times(1)).findById(1L);
        verify(regimeAlimentaireRepository, times(1)).findById(1L);
        verify(allergeneRepository, times(1)).findById(8L);
        verify(typeCuisineRepository, times(1)).findById(2L);
        verify(utilisateurRepository, times(1)).save(utilisateur);
    }

    @Test
    @DisplayName("updateFromDTO - avec préférences vides, devrait vider les collections")
    void updateFromDTO_avecPreferencesVides_devraitViderCollections() {
        // Given
        utilisateur.getRegimes().add(regimeVegetarien);
        utilisateur.getAllergenes().add(allergeneFruitsACoque);
        utilisateur.getTypesCuisinePreferes().add(cuisineItalienne);

        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("john@test.com");
        dto.setNom("Doe");
        dto.setPrenom("John");
        dto.setRegimesIds(Set.of());
        dto.setAllergenesIds(Set.of());
        dto.setTypesCuisinePreferesIds(Set.of());

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // When
        Utilisateur result = utilisateurService.updateFromDTO(1L, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.getRegimes().isEmpty());
        assertTrue(result.getAllergenes().isEmpty());
        assertTrue(result.getTypesCuisinePreferes().isEmpty());
        verify(utilisateurRepository, times(1)).save(utilisateur);
    }

    @Test
    @DisplayName("updateFromDTO - avec ID inexistant, devrait lancer ResourceNotFoundException")
    void updateFromDTO_avecIdInexistant_devraitLancerException() {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("test@test.com");

        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.updateFromDTO(999L, dto)
        );

        assertEquals("Utilisateur non trouvé avec l'ID: 999", exception.getMessage());
        verify(utilisateurRepository, times(1)).findById(999L);
        verify(utilisateurRepository, never()).save(any());
    }
}