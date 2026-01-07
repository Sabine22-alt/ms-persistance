package com.mspersistance.univ.soa.mapper;

import com.mspersistance.univ.soa.dto.UtilisateurDTO;
import com.mspersistance.univ.soa.model.Aliment;
import com.mspersistance.univ.soa.model.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires pour UtilisateurMapper")
class UtilisateurMapperTest {

    private UtilisateurMapper utilisateurMapper;

    @BeforeEach
    void setUp() {
        utilisateurMapper = new UtilisateurMapper();
    }

    // ==================== Tests pour toDTO() ====================

    @Test
    @DisplayName("toDTO - avec entitÃ© complÃ¨te, devrait convertir en DTO")
    void toDTO_avecEntiteComplete_devraitConvertirEnDTO() {
        // Given
        Aliment aliment1 = new Aliment();
        aliment1.setId(1L);
        aliment1.setNom("Arachide");

        Aliment aliment2 = new Aliment();
        aliment2.setId(2L);
        aliment2.setNom("Lait");

        Set<Aliment> alimentsExclus = new HashSet<>();
        alimentsExclus.add(aliment1);
        alimentsExclus.add(aliment2);

        LocalDateTime now = LocalDateTime.now();

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(10L);
        utilisateur.setEmail("john@test.com");
        utilisateur.setNom("Doe");
        utilisateur.setPrenom("John");
        utilisateur.setActif(true);
        utilisateur.setRole(Utilisateur.Role.USER);
        utilisateur.setDateCreation(now);
        utilisateur.setDateModification(now);
        utilisateur.setAlimentsExclus(alimentsExclus);

        // When
        UtilisateurDTO dto = utilisateurMapper.toDTO(utilisateur);

        // Then
        assertNotNull(dto);
        assertEquals(10L, dto.id());
        assertEquals("john@test.com", dto.email());
        assertEquals("Doe", dto.nom());
        assertEquals("John", dto.prenom());
        assertTrue(dto.actif());
        assertEquals(Utilisateur.Role.USER, dto.role());
        assertEquals(now, dto.dateCreation());
        assertEquals(now, dto.dateModification());
        assertNotNull(dto.alimentsExclusIds());
        assertEquals(2, dto.alimentsExclusIds().size());
        assertTrue(dto.alimentsExclusIds().contains(1L));
        assertTrue(dto.alimentsExclusIds().contains(2L));
    }

    @Test
    @DisplayName("toDTO - avec entitÃ© null, devrait retourner null")
    void toDTO_avecEntiteNull_devraitRetournerNull() {
        // When
        UtilisateurDTO dto = utilisateurMapper.toDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("toDTO - sans aliments exclus, liste devrait Ãªtre null")
    void toDTO_sansAlimentsExclus_listeDevraitEtreNull() {
        // Given
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(10L);
        utilisateur.setEmail("john@test.com");
        utilisateur.setAlimentsExclus(null);

        // When
        UtilisateurDTO dto = utilisateurMapper.toDTO(utilisateur);

        // Then
        assertNotNull(dto);
        assertNull(dto.alimentsExclusIds());
    }

    @Test
    @DisplayName("toDTO - avec liste vide d'aliments exclus, devrait retourner liste vide")
    void toDTO_avecListeVideAlimentsExclus_devraitRetournerListeVide() {
        // Given
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(10L);
        utilisateur.setEmail("john@test.com");
        utilisateur.setAlimentsExclus(new HashSet<>());

        // When
        UtilisateurDTO dto = utilisateurMapper.toDTO(utilisateur);

        // Then
        assertNotNull(dto);
        assertNotNull(dto.alimentsExclusIds());
        assertTrue(dto.alimentsExclusIds().isEmpty());
    }

    // ==================== Tests pour toEntity() ====================

    @Test
    @DisplayName("toEntity - avec DTO valide, devrait convertir en entitÃ©")
    void toEntity_avecDTOValide_devraitConvertirEnEntite() {
        Set<Long> alimentsExclusIds = new HashSet<>();
        alimentsExclusIds.add(1L);
        alimentsExclusIds.add(2L);

        UtilisateurDTO dto = new UtilisateurDTO(
            10L, "jane@test.com", "password123", "Smith", "Jane",
            null, null, null, false, Utilisateur.Role.ADMIN,
            null, null, null, alimentsExclusIds, null, null
        );

        Utilisateur utilisateur = utilisateurMapper.toEntity(dto);

        assertNotNull(utilisateur);
        assertEquals(10L, utilisateur.getId());
        assertEquals("jane@test.com", utilisateur.getEmail());
        assertEquals("password123", utilisateur.getMotDePasse());
        assertEquals("Smith", utilisateur.getNom());
        assertEquals("Jane", utilisateur.getPrenom());
        assertFalse(utilisateur.getActif());
        assertEquals(Utilisateur.Role.ADMIN, utilisateur.getRole());
        // Note: alimentsExclus n'est pas mappÃ© dans toEntity (IDs seulement)
        assertTrue(utilisateur.getAlimentsExclus().isEmpty());
    }

    @Test
    @DisplayName("toEntity - avec DTO null, devrait retourner null")
    void toEntity_avecDTONull_devraitRetournerNull() {
        // When
        Utilisateur utilisateur = utilisateurMapper.toEntity(null);

        // Then
        assertNull(utilisateur);
    }

    @Test
    @DisplayName("toEntity - avec ID null, devrait fonctionner")
    void toEntity_avecIdNull_devraitFonctionner() {
        UtilisateurDTO dto = new UtilisateurDTO(
            null, // id
            "new@test.com", // email
            null, // motDePasse
            "New", // nom
            "User", // prenom
            null, // telephone
            null, // bio
            null, // adresse
            null, // actif
            null, // role
            null, // regimesIds
            null, // allergenesIds
            null, // typesCuisinePreferesIds
            null, // alimentsExclusIds
            null, // dateCreation
            null // dateModification
        );
        Utilisateur utilisateur = utilisateurMapper.toEntity(dto);
        assertNotNull(utilisateur);
        assertNull(utilisateur.getId());
        assertEquals("new@test.com", utilisateur.getEmail());
    }

    @Test
    @DisplayName("toEntity - avec mot de passe null, devrait fonctionner")
    void toEntity_avecMotDePasseNull_devraitFonctionner() {
        UtilisateurDTO dto = new UtilisateurDTO(
            null, // id
            "test@test.com", // email
            null, // motDePasse
            null, // nom
            null, // prenom
            null, // telephone
            null, // bio
            null, // adresse
            null, // actif
            null, // role
            null, // regimesIds
            null, // allergenesIds
            null, // typesCuisinePreferesIds
            null, // alimentsExclusIds
            null, // dateCreation
            null // dateModification
        );
        Utilisateur utilisateur = utilisateurMapper.toEntity(dto);
        assertNotNull(utilisateur);
        assertNull(utilisateur.getMotDePasse());
    }

    // ==================== Tests de conversion bidirectionnelle ====================

    @Test
    @DisplayName("conversion bidirectionnelle - devrait prÃ©server les donnÃ©es principales")
    void conversionBidirectionnelle_devraitPreserverLesDonneesPrincipales() {
        // Given
        Utilisateur original = new Utilisateur();
        original.setId(20L);
        original.setEmail("test@example.com");
        original.setMotDePasse("hashedPassword");
        original.setNom("Test");
        original.setPrenom("User");
        original.setActif(true);
        original.setRole(Utilisateur.Role.USER);

        // When
        UtilisateurDTO dto = utilisateurMapper.toDTO(original);
        Utilisateur converti = utilisateurMapper.toEntity(dto);

        // Then
        assertNotNull(converti);
        assertEquals(original.getId(), converti.getId());
        assertEquals(original.getEmail(), converti.getEmail());
        // Note: le mot de passe n'est pas prÃ©sent dans le DTO aprÃ¨s toDTO()
        assertEquals(original.getNom(), converti.getNom());
        assertEquals(original.getPrenom(), converti.getPrenom());
        assertEquals(original.getActif(), converti.getActif());
        assertEquals(original.getRole(), converti.getRole());
    }
}
