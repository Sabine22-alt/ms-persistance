package com.springbootTemplate.univ.soa.mapper;

import com.springbootTemplate.univ.soa.dto.UtilisateurDTO;
import com.springbootTemplate.univ.soa.model.Aliment;
import com.springbootTemplate.univ.soa.model.Utilisateur;
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
    @DisplayName("toDTO - avec entité complète, devrait convertir en DTO")
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
        assertEquals(10L, dto.getId());
        assertEquals("john@test.com", dto.getEmail());
        assertEquals("Doe", dto.getNom());
        assertEquals("John", dto.getPrenom());
        assertTrue(dto.getActif());
        assertEquals(Utilisateur.Role.USER, dto.getRole());
        assertEquals(now, dto.getDateCreation());
        assertEquals(now, dto.getDateModification());
        assertNotNull(dto.getAlimentsExclusIds());
        assertEquals(2, dto.getAlimentsExclusIds().size());
        assertTrue(dto.getAlimentsExclusIds().contains(1L));
        assertTrue(dto.getAlimentsExclusIds().contains(2L));
    }

    @Test
    @DisplayName("toDTO - avec entité null, devrait retourner null")
    void toDTO_avecEntiteNull_devraitRetournerNull() {
        // When
        UtilisateurDTO dto = utilisateurMapper.toDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("toDTO - sans aliments exclus, liste devrait être null")
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
        assertNull(dto.getAlimentsExclusIds());
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
        assertNotNull(dto.getAlimentsExclusIds());
        assertTrue(dto.getAlimentsExclusIds().isEmpty());
    }

    // ==================== Tests pour toEntity() ====================

    @Test
    @DisplayName("toEntity - avec DTO valide, devrait convertir en entité")
    void toEntity_avecDTOValide_devraitConvertirEnEntite() {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(10L);
        dto.setEmail("jane@test.com");
        dto.setMotDePasse("password123");
        dto.setNom("Smith");
        dto.setPrenom("Jane");
        dto.setActif(false);
        dto.setRole(Utilisateur.Role.ADMIN);

        Set<Long> alimentsExclusIds = new HashSet<>();
        alimentsExclusIds.add(1L);
        alimentsExclusIds.add(2L);
        dto.setAlimentsExclusIds(alimentsExclusIds);

        // When
        Utilisateur utilisateur = utilisateurMapper.toEntity(dto);

        // Then
        assertNotNull(utilisateur);
        assertEquals(10L, utilisateur.getId());
        assertEquals("jane@test.com", utilisateur.getEmail());
        assertEquals("password123", utilisateur.getMotDePasse());
        assertEquals("Smith", utilisateur.getNom());
        assertEquals("Jane", utilisateur.getPrenom());
        assertFalse(utilisateur.getActif());
        assertEquals(Utilisateur.Role.ADMIN, utilisateur.getRole());
        // Note: alimentsExclus n'est pas mappé dans toEntity (IDs seulement)
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
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("new@test.com");
        dto.setNom("New");
        dto.setPrenom("User");

        // When
        Utilisateur utilisateur = utilisateurMapper.toEntity(dto);

        // Then
        assertNotNull(utilisateur);
        assertNull(utilisateur.getId());
        assertEquals("new@test.com", utilisateur.getEmail());
    }

    @Test
    @DisplayName("toEntity - avec mot de passe null, devrait fonctionner")
    void toEntity_avecMotDePasseNull_devraitFonctionner() {
        // Given
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setEmail("test@test.com");
        dto.setMotDePasse(null);

        // When
        Utilisateur utilisateur = utilisateurMapper.toEntity(dto);

        // Then
        assertNotNull(utilisateur);
        assertNull(utilisateur.getMotDePasse());
    }

    // ==================== Tests de conversion bidirectionnelle ====================

    @Test
    @DisplayName("conversion bidirectionnelle - devrait préserver les données principales")
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
        // Note: le mot de passe n'est pas présent dans le DTO après toDTO()
        assertEquals(original.getNom(), converti.getNom());
        assertEquals(original.getPrenom(), converti.getPrenom());
        assertEquals(original.getActif(), converti.getActif());
        assertEquals(original.getRole(), converti.getRole());
    }
}