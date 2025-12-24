package com.springbootTemplate.univ.soa.mapper;

import com.springbootTemplate.univ.soa.dto.AlimentDTO;
import com.springbootTemplate.univ.soa.model.Aliment;
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
    @DisplayName("toDTO - avec entité valide, devrait convertir en DTO")
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
        assertEquals(1L, dto.getId());
        assertEquals("Tomate", dto.getNom());
        assertEquals(Aliment.CategorieAliment.LEGUME, dto.getCategorieAliment());
    }

    @Test
    @DisplayName("toDTO - avec entité null, devrait retourner null")
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
        assertNull(dto.getId());
        assertEquals("Carotte", dto.getNom());
    }

    // ==================== Tests pour toEntity() ====================

    @Test
    @DisplayName("toEntity - avec DTO valide, devrait convertir en entité")
    void toEntity_avecDTOValide_devraitConvertirEnEntite() {
        // Given
        AlimentDTO dto = new AlimentDTO();
        dto.setId(1L);
        dto.setNom("Pomme");
        dto.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        // When
        Aliment aliment = alimentMapper.toEntity(dto);

        // Then
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
        // Given
        AlimentDTO dto = new AlimentDTO();
        dto.setNom("Banane");
        dto.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        // When
        Aliment aliment = alimentMapper.toEntity(dto);

        // Then
        assertNotNull(aliment);
        assertNull(aliment.getId());
        assertEquals("Banane", aliment.getNom());
    }

    // ==================== Tests de conversion bidirectionnelle ====================

    @Test
    @DisplayName("conversion bidirectionnelle - devrait préserver les données")
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