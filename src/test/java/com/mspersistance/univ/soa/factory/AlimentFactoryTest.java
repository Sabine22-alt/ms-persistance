package com.mspersistance.univ.soa.factory;

import com.mspersistance.univ.soa.dto.AlimentDTO;
import com.mspersistance.univ.soa.model.Aliment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires pour AlimentFactory")
class AlimentFactoryTest {

    private final AlimentFactory alimentFactory = new AlimentFactory();

    @Test
    @DisplayName("createFromDTO - avec DTO complet, devrait crÃ©er aliment")
    void createFromDTO_avecDTOComplet_devraitCreerAliment() {
        AlimentDTO dto = new AlimentDTO(
            null, "Pomme", 52f, 0.3f, 14f, 0.2f, 2.4f, Aliment.CategorieAliment.FRUIT
        );

        Aliment aliment = alimentFactory.createFromDTO(dto);

        assertNotNull(aliment);
        assertEquals("Pomme", aliment.getNom());
        assertEquals(52f, aliment.getCalories());
        assertEquals(0.3f, aliment.getProteines());
        assertEquals(14f, aliment.getGlucides());
        assertEquals(0.2f, aliment.getLipides());
        assertEquals(2.4f, aliment.getFibres());
        assertEquals(Aliment.CategorieAliment.FRUIT, aliment.getCategorieAliment());
    }

    @Test
    @DisplayName("createFromDTO - avec valeurs nutritionnelles null, devrait fonctionner")
    void createFromDTO_avecValeursNutritionnellesNull_devraitFonctionner() {
        AlimentDTO dto = new AlimentDTO(
            null, "Tomate", null, null, null, null, null, Aliment.CategorieAliment.LEGUME
        );

        Aliment aliment = alimentFactory.createFromDTO(dto);

        assertNotNull(aliment);
        assertEquals("Tomate", aliment.getNom());
        assertNull(aliment.getCalories());
        assertNull(aliment.getProteines());
        assertEquals(Aliment.CategorieAliment.LEGUME, aliment.getCategorieAliment());
    }

    @Test
    @DisplayName("updateFromDTO - devrait mettre Ã  jour aliment existant")
    void updateFromDTO_devraitMettreAJourAlimentExistant() {
        Aliment existing = new Aliment();
        existing.setId(1L);
        existing.setNom("Ancien nom");
        existing.setCalories(100f);
        existing.setCategorieAliment(Aliment.CategorieAliment.FRUIT);

        AlimentDTO dto = new AlimentDTO(
            null, "Nouveau nom", 150f, 5f, 30f, 2f, 3f, Aliment.CategorieAliment.LEGUME
        );

        Aliment updated = alimentFactory.updateFromDTO(existing, dto);

        assertNotNull(updated);
        assertEquals(1L, updated.getId());
        assertEquals("Nouveau nom", updated.getNom());
        assertEquals(150f, updated.getCalories());
        assertEquals(5f, updated.getProteines());
        assertEquals(Aliment.CategorieAliment.LEGUME, updated.getCategorieAliment());
    }

    @Test
    @DisplayName("updateFromDTO - avec nom null, devrait conserver ancien nom")
    void updateFromDTO_avecNomNull_devraitConserverAncienNom() {
        Aliment existing = new Aliment();
        existing.setId(1L);
        existing.setNom("Nom original");
        existing.setCalories(100f);

        AlimentDTO dto = new AlimentDTO(
            null, null, 200f, null, null, null, null, null
        );

        Aliment updated = alimentFactory.updateFromDTO(existing, dto);

        assertEquals("Nom original", updated.getNom());
        assertEquals(200f, updated.getCalories());
    }
}

