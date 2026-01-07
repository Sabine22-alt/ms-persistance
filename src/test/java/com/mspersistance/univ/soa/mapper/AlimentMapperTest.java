package com.mspersistance.univ.soa.mapper;

import com.mspersistance.univ.soa.dto.AlimentDTO;
import com.mspersistance.univ.soa.model.Aliment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires pour AlimentMapper")
class AlimentMapperTest {

    private AlimentMapper alimentMapper;

    @BeforeEach
    void setUp() {
        alimentMapper = new AlimentMapper();
    }

    // ==================== Tests pour toDTO() ====================

    @Test
    @DisplayName("toDTO - avec entitÃ© valide, devrait convertir en DTO")
    void toDTO_avecEntiteValide_devraitConvertirEnDTO() {
        // Given
        Aliment aliment = new Aliment();
        aliment.setId(1L);
        aliment.setNom("Tomate");
        aliment.setCategorieAliment(Aliment.CategorieAliment.LEGUME);

        // When
        AlimentDTO dto = alimentMapper.toDTO(aliment);

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.id());
        assertEquals("Tomate", dto.nom());
        assertEquals(Aliment.CategorieAliment.LEGUME, dto.categorieAliment());
    }

    @Test
    @DisplayName("toDTO - avec entitÃ© null, devrait retourner null")
    void toDTO_avecEntiteNull_devraitRetournerNull() {
        // When
        AlimentDTO dto = alimentMapper.toDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("toDTO - avec ID null, devrait fonctionner")
    void toDTO_avecIdNull_devraitFonctionner() {
        // Given
        Aliment aliment = new Aliment();
        aliment.setNom("Carotte");
        aliment.setCategorieAliment(Aliment.CategorieAliment.LEGUME);

        // When
        AlimentDTO dto = alimentMapper.toDTO(aliment);

        // Then
        assertNotNull(dto);
        assertNull(dto.id());
        assertEquals("Carotte", dto.nom());
    }

    // ==================== Tests pour toEntity() ====================

    @Test
    @DisplayName("toEntity - avec DTO valide, devrait convertir en entitÃ©")
    void toEntity_avecDTOValide_devraitConvertirEnEntite() {
        AlimentDTO dto = new AlimentDTO(1L, "Pomme", null, null, null, null, null, Aliment.CategorieAliment.FRUIT);
        Aliment aliment = alimentMapper.toEntity(dto);
        assertNotNull(aliment);
        assertEquals(1L, aliment.getId());
        assertEquals("Pomme", aliment.getNom());
        assertEquals(Aliment.CategorieAliment.FRUIT, aliment.getCategorieAliment());
    }

    @Test
    @DisplayName("toEntity - avec DTO null, devrait retourner null")
    void toEntity_avecDTONull_devraitRetournerNull() {
        // When
        Aliment aliment = alimentMapper.toEntity(null);

        // Then
        assertNull(aliment);
    }

    @Test
    @DisplayName("toEntity - avec ID null, devrait fonctionner")
    void toEntity_avecIdNull_devraitFonctionner() {
        AlimentDTO dto = new AlimentDTO(null, "Banane", null, null, null, null, null, Aliment.CategorieAliment.FRUIT);
        Aliment aliment = alimentMapper.toEntity(dto);
        assertNotNull(aliment);
        assertNull(aliment.getId());
        assertEquals("Banane", aliment.getNom());
    }

    // ==================== Tests de conversion bidirectionnelle ====================

    @Test
    @DisplayName("conversion bidirectionnelle - devrait prÃ©server les donnÃ©es")
    void conversionBidirectionnelle_devraitPreserverLesDonnees() {
        // Given
        Aliment original = new Aliment();
        original.setId(5L);
        original.setNom("Poulet");
        original.setCategorieAliment(Aliment.CategorieAliment.VIANDE);

        // When
        AlimentDTO dto = alimentMapper.toDTO(original);
        Aliment converti = alimentMapper.toEntity(dto);

        // Then
        assertNotNull(converti);
        assertEquals(original.getId(), converti.getId());
        assertEquals(original.getNom(), converti.getNom());
        assertEquals(original.getCategorieAliment(), converti.getCategorieAliment());
    }
}
