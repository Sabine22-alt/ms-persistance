package com.mspersistance.univ.soa.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mspersistance.univ.soa.dto.UtilisateurDTO;
import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.factory.UtilisateurFactory;
import com.mspersistance.univ.soa.model.Utilisateur;
import com.mspersistance.univ.soa.repository.AlimentRepository;
import com.mspersistance.univ.soa.repository.AllergeneRepository;
import com.mspersistance.univ.soa.repository.PasswordResetTokenRepository;
import com.mspersistance.univ.soa.repository.RegimeAlimentaireRepository;
import com.mspersistance.univ.soa.repository.TypeCuisineRepository;
import com.mspersistance.univ.soa.repository.UtilisateurRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour UtilisateurService")
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private AlimentRepository alimentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private RegimeAlimentaireRepository regimeAlimentaireRepository;

    @Mock
    private AllergeneRepository allergeneRepository;

    @Mock
    private TypeCuisineRepository typeCuisineRepository;

    @Mock
    private UtilisateurFactory utilisateurFactory;

    @InjectMocks
    private UtilisateurService utilisateurService;

    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        utilisateur = Utilisateur.builder()
                .id(1L)
                .email("john@test.com")
                .nom("Doe")
                .prenom("John")
                .motDePasse("hashedPassword123")
                .actif(true)
                .role(Utilisateur.Role.USER)
                .regimesAlimentaires(new HashSet<>())
                .allergenes(new HashSet<>())
                .typesCuisinePreferences(new HashSet<>())
                .alimentsExclus(new HashSet<>())
                .build();
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
    @DisplayName("save - avec utilisateur valide, devrait hasher le mot de passe et crÃ©er l'utilisateur")
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
    @DisplayName("save - devrait dÃ©finir les valeurs par dÃ©faut si nulles")
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
    @DisplayName("save - devrait mettre l'ID Ã  null")
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

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("delete - avec ID existant, devrait supprimer l'utilisateur")
    void delete_avecIdExistant_devraitSupprimerUtilisateur() {
        // Given
        when(utilisateurRepository.existsById(1L)).thenReturn(true);
        doNothing().when(utilisateurRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> utilisateurService.delete(1L));

        // Then
        verify(utilisateurRepository, times(1)).existsById(1L);
        verify(utilisateurRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("delete - avec ID inexistant, devrait lancer ResourceNotFoundException")
    void delete_avecIdInexistant_devraitLancerException() {
        // Given
        when(utilisateurRepository.existsById(999L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.delete(999L)
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
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("new@test.com")
                .nom("New")
                .prenom("User")
                .motDePasse("plainPassword")
                .regimesIds(Set.of(1L))
                .allergenesIds(Set.of(8L))
                .typesCuisinePreferesIds(Set.of(2L))
                .build();

        Utilisateur utilisateurCree = new Utilisateur();
        utilisateurCree.setEmail("new@test.com");
        utilisateurCree.setNom("New");
        utilisateurCree.setPrenom("User");

        Utilisateur utilisateurSauvegarde = new Utilisateur();
        utilisateurSauvegarde.setId(2L);
        utilisateurSauvegarde.setEmail("new@test.com");

        when(regimeAlimentaireRepository.existsById(1L)).thenReturn(true);
        when(allergeneRepository.existsById(8L)).thenReturn(true);
        when(typeCuisineRepository.existsById(2L)).thenReturn(true);
        when(utilisateurFactory.createFromDTO(dto)).thenReturn(utilisateurCree);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurSauvegarde);

        // When
        Utilisateur result = utilisateurService.saveFromDTO(dto);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(regimeAlimentaireRepository, times(1)).existsById(1L);
        verify(allergeneRepository, times(1)).existsById(8L);
        verify(typeCuisineRepository, times(1)).existsById(2L);
        verify(utilisateurFactory, times(1)).createFromDTO(dto);
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("saveFromDTO - avec régime inexistant, devrait lancer ResourceNotFoundException")
    void saveFromDTO_avecRegimeInexistant_devraitLancerException() {
        // Given
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("new@test.com")
                .nom("New")
                .prenom("User")
                .motDePasse("plainPassword")
                .regimesIds(Set.of(999L))
                .build();

        when(regimeAlimentaireRepository.existsById(999L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.saveFromDTO(dto)
        );

        assertEquals("Régime alimentaire non trouvé avec l'ID: 999", exception.getMessage());
        verify(regimeAlimentaireRepository, times(1)).existsById(999L);
        verify(utilisateurFactory, never()).createFromDTO(any());
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveFromDTO - avec allergène inexistant, devrait lancer ResourceNotFoundException")
    void saveFromDTO_avecAllergeneInexistant_devraitLancerException() {
        // Given
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("new@test.com")
                .nom("New")
                .prenom("User")
                .motDePasse("plainPassword")
                .allergenesIds(Set.of(999L))
                .build();

        when(allergeneRepository.existsById(999L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.saveFromDTO(dto)
        );

        assertEquals("Allergène non trouvé avec l'ID: 999", exception.getMessage());
        verify(allergeneRepository, times(1)).existsById(999L);
        verify(utilisateurFactory, never()).createFromDTO(any());
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveFromDTO - avec type de cuisine inexistant, devrait lancer ResourceNotFoundException")
    void saveFromDTO_avecTypeCuisineInexistant_devraitLancerException() {
        // Given
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("new@test.com")
                .nom("New")
                .prenom("User")
                .motDePasse("plainPassword")
                .typesCuisinePreferesIds(Set.of(999L))
                .build();

        when(typeCuisineRepository.existsById(999L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.saveFromDTO(dto)
        );

        assertEquals("Type de cuisine non trouvé avec l'ID: 999", exception.getMessage());
        verify(typeCuisineRepository, times(1)).existsById(999L);
        verify(utilisateurFactory, never()).createFromDTO(any());
        verify(utilisateurRepository, never()).save(any());
    }

    // ==================== Tests pour updateFromDTO() ====================

    @Test
    @DisplayName("updateFromDTO - avec préférences alimentaires, devrait mettre à jour l'utilisateur")
    void updateFromDTO_avecPreferencesAlimentaires_devraitMettreAJourUtilisateur() {
        // Given
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("john@test.com")
                .nom("Doe Updated")
                .prenom("John Updated")
                .regimesIds(Set.of(1L))
                .allergenesIds(Set.of(8L))
                .typesCuisinePreferesIds(Set.of(2L))
                .build();

        Utilisateur utilisateurMisAJour = new Utilisateur();
        utilisateurMisAJour.setId(1L);
        utilisateurMisAJour.setEmail("john@test.com");
        utilisateurMisAJour.setNom("Doe Updated");
        utilisateurMisAJour.setPrenom("John Updated");

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurFactory.updateFromDTO(utilisateur, dto)).thenReturn(utilisateurMisAJour);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurMisAJour);

        // When
        Utilisateur result = utilisateurService.updateFromDTO(1L, dto);

        // Then
        assertNotNull(result);
        assertEquals("Doe Updated", result.getNom());
        assertEquals("John Updated", result.getPrenom());
        verify(utilisateurRepository, times(1)).findById(1L);
        verify(utilisateurFactory, times(1)).updateFromDTO(utilisateur, dto);
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("updateFromDTO - avec préférences vides, devrait vider les collections")
    void updateFromDTO_avecPreferencesVides_devraitViderCollections() {
        // Given
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("john@test.com")
                .nom("Doe")
                .prenom("John")
                .regimesIds(Set.of())
                .allergenesIds(Set.of())
                .typesCuisinePreferesIds(Set.of())
                .build();

        Utilisateur utilisateurMisAJour = new Utilisateur();
        utilisateurMisAJour.setId(1L);
        utilisateurMisAJour.setEmail("john@test.com");
        utilisateurMisAJour.setRegimesAlimentaires(new HashSet<>());
        utilisateurMisAJour.setAllergenes(new HashSet<>());
        utilisateurMisAJour.setTypesCuisinePreferences(new HashSet<>());

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurFactory.updateFromDTO(utilisateur, dto)).thenReturn(utilisateurMisAJour);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateurMisAJour);

        // When
        Utilisateur result = utilisateurService.updateFromDTO(1L, dto);

        // Then
        assertNotNull(result);
        assertTrue(result.getRegimesAlimentaires().isEmpty());
        assertTrue(result.getAllergenes().isEmpty());
        assertTrue(result.getTypesCuisinePreferences().isEmpty());
        verify(utilisateurFactory, times(1)).updateFromDTO(utilisateur, dto);
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    @DisplayName("updateFromDTO - avec ID inexistant, devrait lancer ResourceNotFoundException")
    void updateFromDTO_avecIdInexistant_devraitLancerException() {
        // Given
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .email("test@test.com")
                .build();

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
