package com.mspersistance.univ.soa.factory;

import com.mspersistance.univ.soa.dto.UtilisateurDTO;
import com.mspersistance.univ.soa.model.Utilisateur;
import com.mspersistance.univ.soa.repository.AlimentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour UtilisateurFactory")
class UtilisateurFactoryTest {

    @Mock
    private AlimentRepository alimentRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UtilisateurFactory utilisateurFactory;

    @BeforeEach
    void setUp() {
        utilisateurFactory = new UtilisateurFactory(passwordEncoder, alimentRepository);
    }

    @Test
    @DisplayName("createFromDTO - avec DTO complet, devrait créer utilisateur")
    void createFromDTO_avecDTOComplet_devraitCreerUtilisateur() {
        Set<Long> alimentsExclusIds = new HashSet<>();
        alimentsExclusIds.add(1L);
        alimentsExclusIds.add(2L);

        UtilisateurDTO dto = new UtilisateurDTO(
            null, "john@test.com", "password123", "Doe", "John",
            "0612345678", "Développeur Java", "123 rue Test",
            true, Utilisateur.Role.USER, alimentsExclusIds, null, null
        );

        Utilisateur utilisateur = utilisateurFactory.createFromDTO(dto);

        assertNotNull(utilisateur);
        assertEquals("john@test.com", utilisateur.getEmail());
        assertEquals("Doe", utilisateur.getNom());
        assertEquals("John", utilisateur.getPrenom());
        assertEquals("0612345678", utilisateur.getTelephone());
        assertEquals("Développeur Java", utilisateur.getBio());
        assertEquals("123 rue Test", utilisateur.getAdresse());
        assertTrue(utilisateur.getActif());
        assertEquals(Utilisateur.Role.USER, utilisateur.getRole());
        assertNotNull(utilisateur.getMotDePasse());
        assertNotEquals("password123", utilisateur.getMotDePasse());
        assertTrue(passwordEncoder.matches("password123", utilisateur.getMotDePasse()));
    }

    @Test
    @DisplayName("createFromDTO - avec mot de passe, devrait être encodé")
    void createFromDTO_avecMotDePasse_devraitEtreEncode() {
        UtilisateurDTO dto = new UtilisateurDTO(
            null, "test@test.com", "plainPassword", "Test", "User",
            null, null, null, null, null, null, null, null
        );

        Utilisateur utilisateur = utilisateurFactory.createFromDTO(dto);

        assertNotNull(utilisateur.getMotDePasse());
        assertNotEquals("plainPassword", utilisateur.getMotDePasse());
        assertTrue(passwordEncoder.matches("plainPassword", utilisateur.getMotDePasse()));
    }

    @Test
    @DisplayName("createFromDTO - avec champs optionnels null, devrait fonctionner")
    void createFromDTO_avecChampsOptionnelsNull_devraitFonctionner() {
        UtilisateurDTO dto = new UtilisateurDTO(
            null, "minimal@test.com", "password", "Nom", "Prenom",
            null, null, null, null, null, null, null, null
        );

        Utilisateur utilisateur = utilisateurFactory.createFromDTO(dto);

        assertNotNull(utilisateur);
        assertNull(utilisateur.getTelephone());
        assertNull(utilisateur.getBio());
        assertNull(utilisateur.getAdresse());
    }

    @Test
    @DisplayName("updateFromDTO - devrait mettre à jour utilisateur existant")
    void updateFromDTO_devraitMettreAJourUtilisateurExistant() {
        Utilisateur existing = new Utilisateur();
        existing.setId(1L);
        existing.setEmail("old@test.com");
        existing.setMotDePasse("oldPassword");
        existing.setNom("OldNom");
        existing.setPrenom("OldPrenom");

        UtilisateurDTO dto = new UtilisateurDTO(
            null, "new@test.com", null, "NewNom", "NewPrenom",
            "0612345678", "Ma bio", "Nouvelle adresse",
            true, Utilisateur.Role.ADMIN, null, null, null
        );

        Utilisateur updated = utilisateurFactory.updateFromDTO(existing, dto);

        assertNotNull(updated);
        assertEquals("new@test.com", updated.getEmail());
        assertEquals("NewNom", updated.getNom());
        assertEquals("NewPrenom", updated.getPrenom());
        assertEquals("0612345678", updated.getTelephone());
        assertEquals("Ma bio", updated.getBio());
        assertEquals("Nouvelle adresse", updated.getAdresse());
        assertEquals("oldPassword", updated.getMotDePasse());
        assertEquals(Utilisateur.Role.ADMIN, updated.getRole());
    }

    @Test
    @DisplayName("updateFromDTO - avec nouveau mot de passe, devrait encoder")
    void updateFromDTO_avecNouveauMotDePasse_devraitEncoder() {
        Utilisateur existing = new Utilisateur();
        existing.setId(1L);
        existing.setEmail("test@test.com");
        existing.setMotDePasse("oldEncodedPassword");

        UtilisateurDTO dto = new UtilisateurDTO(
            null, "test@test.com", "newPassword", "Nom", "Prenom",
            null, null, null, null, null, null, null, null
        );

        Utilisateur updated = utilisateurFactory.updateFromDTO(existing, dto);

        assertNotNull(updated.getMotDePasse());
        assertNotEquals("oldEncodedPassword", updated.getMotDePasse());
        assertTrue(passwordEncoder.matches("newPassword", updated.getMotDePasse()));
    }
}

