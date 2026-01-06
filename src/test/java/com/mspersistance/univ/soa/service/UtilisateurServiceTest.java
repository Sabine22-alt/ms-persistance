package com.mspersistance.univ.soa.service;

import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.model.Aliment;
import com.mspersistance.univ.soa.model.Utilisateur;
import com.mspersistance.univ.soa.repository.AlimentRepository;
import com.mspersistance.univ.soa.repository.UtilisateurRepository;
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
    private AlimentRepository alimentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilisateurService utilisateurService;

    private Utilisateur utilisateur;
    private Aliment aliment;

    @BeforeEach
    void setUp() {
        aliment = new Aliment();
        aliment.setId(1L);
        aliment.setNom("Pâtes");
        aliment.setCategorieAliment(Aliment.CategorieAliment.GLUTEN);

        utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setEmail("john@test.com");
        utilisateur.setNom("Doe");
        utilisateur.setPrenom("John");
        utilisateur.setMotDePasse("hashedPassword123");
        utilisateur.setActif(true);
        utilisateur.setRole(Utilisateur.Role.USER);
        utilisateur.setAlimentsExclus(new HashSet<>());
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

    // ==================== Tests pour update() ====================

    @Test
    @DisplayName("update - avec ID existant, devrait mettre à jour l'utilisateur")
    void update_avecIdExistant_devraitMettreAJourUtilisateur() {
        // Given
        Utilisateur utilisateurMisAJour = new Utilisateur();
        utilisateurMisAJour.setEmail("updated@test.com");
        utilisateurMisAJour.setNom("UpdatedNom");
        utilisateurMisAJour.setPrenom("UpdatedPrenom");
        utilisateurMisAJour.setActif(false);
        utilisateurMisAJour.setRole(Utilisateur.Role.ADMIN);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // When
        Utilisateur result = utilisateurService.update(1L, utilisateurMisAJour);

        // Then
        assertNotNull(result);
        assertEquals("updated@test.com", result.getEmail());
        assertEquals("UpdatedNom", result.getNom());
        assertEquals("UpdatedPrenom", result.getPrenom());
        assertFalse(result.getActif());
        assertEquals(Utilisateur.Role.ADMIN, result.getRole());
        verify(utilisateurRepository, times(1)).findById(1L);
        verify(utilisateurRepository, times(1)).save(utilisateur);
    }

    @Test
    @DisplayName("update - devrait hasher le nouveau mot de passe s'il est fourni")
    void update_devraitHasherNouveauMotDePasseSiFourni() {
        // Given
        Utilisateur utilisateurMisAJour = new Utilisateur();
        utilisateurMisAJour.setEmail("john@test.com");
        utilisateurMisAJour.setNom("Doe");
        utilisateurMisAJour.setPrenom("John");
        utilisateurMisAJour.setMotDePasse("newPlainPassword");

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.encode("newPlainPassword")).thenReturn("newHashedPassword");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // When
        Utilisateur result = utilisateurService.update(1L, utilisateurMisAJour);

        // Then
        assertEquals("newHashedPassword", result.getMotDePasse());
        verify(passwordEncoder, times(1)).encode("newPlainPassword");
        verify(utilisateurRepository, times(1)).save(utilisateur);
    }

    @Test
    @DisplayName("update - avec ID inexistant, devrait lancer ResourceNotFoundException")
    void update_avecIdInexistant_devraitLancerException() {
        // Given
        Utilisateur utilisateurMisAJour = new Utilisateur();
        utilisateurMisAJour.setEmail("test@test.com");

        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.update(999L, utilisateurMisAJour)
        );

        assertEquals("Utilisateur non trouvé avec l'ID: 999", exception.getMessage());
        verify(utilisateurRepository, times(1)).findById(999L);
        verify(utilisateurRepository, never()).save(any());
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

    // ==================== Tests pour addAlimentExclu() ====================

    @Test
    @DisplayName("addAlimentExclu - devrait ajouter un aliment exclu à l'utilisateur")
    void addAlimentExclu_devraitAjouterAlimentExclu() {
        // Given
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(alimentRepository.findById(1L)).thenReturn(Optional.of(aliment));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // When
        utilisateurService.addAlimentExclu(1L, 1L);

        // Then
        assertTrue(utilisateur.getAlimentsExclus().contains(aliment));
        verify(utilisateurRepository, times(1)).findById(1L);
        verify(alimentRepository, times(1)).findById(1L);
        verify(utilisateurRepository, times(1)).save(utilisateur);
    }

    @Test
    @DisplayName("addAlimentExclu - avec utilisateur inexistant, devrait lancer ResourceNotFoundException")
    void addAlimentExclu_avecUtilisateurInexistant_devraitLancerException() {
        // Given
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.addAlimentExclu(999L, 1L)
        );

        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(utilisateurRepository, times(1)).findById(999L);
        verify(alimentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("addAlimentExclu - avec aliment inexistant, devrait lancer ResourceNotFoundException")
    void addAlimentExclu_avecAlimentInexistant_devraitLancerException() {
        // Given
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(alimentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.addAlimentExclu(1L, 999L)
        );

        assertEquals("Aliment non trouvé", exception.getMessage());
        verify(utilisateurRepository, times(1)).findById(1L);
        verify(alimentRepository, times(1)).findById(999L);
    }

    // ==================== Tests pour removeAlimentExclu() ====================

    @Test
    @DisplayName("removeAlimentExclu - devrait retirer un aliment exclu de l'utilisateur")
    void removeAlimentExclu_devraitRetirerAlimentExclu() {
        // Given
        utilisateur.getAlimentsExclus().add(aliment);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // When
        utilisateurService.removeAlimentExclu(1L, 1L);

        // Then
        assertFalse(utilisateur.getAlimentsExclus().contains(aliment));
        verify(utilisateurRepository, times(1)).findById(1L);
        verify(utilisateurRepository, times(1)).save(utilisateur);
    }

    @Test
    @DisplayName("removeAlimentExclu - avec utilisateur inexistant, devrait lancer ResourceNotFoundException")
    void removeAlimentExclu_avecUtilisateurInexistant_devraitLancerException() {
        // Given
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> utilisateurService.removeAlimentExclu(999L, 1L)
        );

        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(utilisateurRepository, times(1)).findById(999L);
    }
}