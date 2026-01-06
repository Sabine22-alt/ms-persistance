package com.mspersistance.univ.soa.service;

import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.model.Aliment;
import com.mspersistance.univ.soa.model.Aliment.CategorieAliment;
import com.mspersistance.univ.soa.repository.AlimentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour AlimentService")
class AlimentServiceTest {

    @Mock
    private AlimentRepository alimentRepository;

    @InjectMocks
    private AlimentService alimentService;

    private Aliment aliment;

    @BeforeEach
    void setUp() {
        aliment = new Aliment();
        aliment.setId(1L);
        aliment.setNom("Tomate");
        aliment.setCategorieAliment(CategorieAliment.LEGUME);
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("findAll - devrait retourner tous les aliments")
    void findAll_devraitRetournerTousLesAliments() {
        // Given
        Aliment aliment2 = new Aliment();
        aliment2.setId(2L);
        aliment2.setNom("Carotte");
        aliment2.setCategorieAliment(CategorieAliment.LEGUME);

        List<Aliment> aliments = Arrays.asList(aliment, aliment2);
        when(alimentRepository.findAll()).thenReturn(aliments);

        // When
        List<Aliment> result = alimentService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tomate", result.get(0).getNom());
        assertEquals("Carotte", result.get(1).getNom());
        verify(alimentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll - devrait retourner une liste vide si aucun aliment")
    void findAll_devraitRetournerListeVideSiAucunAliment() {
        // Given
        when(alimentRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Aliment> result = alimentService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(alimentRepository, times(1)).findAll();
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("findById - avec ID existant, devrait retourner l'aliment")
    void findById_avecIdExistant_devraitRetournerAliment() {
        // Given
        when(alimentRepository.findById(1L)).thenReturn(Optional.of(aliment));

        // When
        Optional<Aliment> result = alimentService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Tomate", result.get().getNom());
        assertEquals(CategorieAliment.LEGUME, result.get().getCategorieAliment());
        verify(alimentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById - avec ID inexistant, devrait retourner Optional vide")
    void findById_avecIdInexistant_devraitRetournerOptionalVide() {
        // Given
        when(alimentRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Aliment> result = alimentService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(alimentRepository, times(1)).findById(999L);
    }

    // ==================== Tests pour findByNom() ====================

    @Test
    @DisplayName("findByNom - avec nom existant, devrait retourner l'aliment")
    void findByNom_avecNomExistant_devraitRetournerAliment() {
        // Given
        when(alimentRepository.findByNomIgnoreCase("Tomate")).thenReturn(Optional.of(aliment));

        // When
        Optional<Aliment> result = alimentService.findByNom("Tomate");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Tomate", result.get().getNom());
        verify(alimentRepository, times(1)).findByNomIgnoreCase("Tomate");
    }

    @Test
    @DisplayName("findByNom - devrait ignorer la casse")
    void findByNom_devraitIgnorerLaCasse() {
        // Given
        when(alimentRepository.findByNomIgnoreCase("TOMATE")).thenReturn(Optional.of(aliment));

        // When
        Optional<Aliment> result = alimentService.findByNom("TOMATE");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Tomate", result.get().getNom());
        verify(alimentRepository, times(1)).findByNomIgnoreCase("TOMATE");
    }

    @Test
    @DisplayName("findByNom - avec nom inexistant, devrait retourner Optional vide")
    void findByNom_avecNomInexistant_devraitRetournerOptionalVide() {
        // Given
        when(alimentRepository.findByNomIgnoreCase("Inconnu")).thenReturn(Optional.empty());

        // When
        Optional<Aliment> result = alimentService.findByNom("Inconnu");

        // Then
        assertFalse(result.isPresent());
        verify(alimentRepository, times(1)).findByNomIgnoreCase("Inconnu");
    }

    // ==================== Tests pour save() ====================

    @Test
    @DisplayName("save - avec aliment valide, devrait créer et retourner l'aliment")
    void save_avecAlimentValide_devraitCreerEtRetournerAliment() {
        // Given
        Aliment nouveauAliment = new Aliment();
        nouveauAliment.setNom("Poivron");
        nouveauAliment.setCategorieAliment(CategorieAliment.LEGUME);

        Aliment alimentSauvegarde = new Aliment();
        alimentSauvegarde.setId(3L);
        alimentSauvegarde.setNom("Poivron");
        alimentSauvegarde.setCategorieAliment(CategorieAliment.LEGUME);

        when(alimentRepository.save(any(Aliment.class))).thenReturn(alimentSauvegarde);

        // When
        Aliment result = alimentService.save(nouveauAliment);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Poivron", result.getNom());
        assertEquals(CategorieAliment.LEGUME, result.getCategorieAliment());
        assertNull(nouveauAliment.getId()); // Vérifie que l'ID est bien mis à null avant save
        verify(alimentRepository, times(1)).save(any(Aliment.class));
    }

    @Test
    @DisplayName("save - devrait mettre l'ID à null même si fourni")
    void save_devraitMettreIdANullMemeSiFourni() {
        // Given
        Aliment alimentAvecId = new Aliment();
        alimentAvecId.setId(999L); // ID existant
        alimentAvecId.setNom("Courgette");
        alimentAvecId.setCategorieAliment(CategorieAliment.LEGUME);

        Aliment alimentSauvegarde = new Aliment();
        alimentSauvegarde.setId(4L); // Nouvel ID généré
        alimentSauvegarde.setNom("Courgette");
        alimentSauvegarde.setCategorieAliment(CategorieAliment.LEGUME);

        when(alimentRepository.save(any(Aliment.class))).thenReturn(alimentSauvegarde);

        // When
        Aliment result = alimentService.save(alimentAvecId);

        // Then
        assertNotNull(result);
        assertEquals(4L, result.getId()); // Nouvel ID
        assertNull(alimentAvecId.getId()); // ID original mis à null
        verify(alimentRepository, times(1)).save(any(Aliment.class));
    }

    // ==================== Tests pour update() ====================

    @Test
    @DisplayName("update - avec ID existant, devrait mettre à jour l'aliment")
    void update_avecIdExistant_devraitMettreAJourAliment() {
        // Given
        Aliment alimentMisAJour = new Aliment();
        alimentMisAJour.setNom("Tomate cerise");
        alimentMisAJour.setCategorieAliment(CategorieAliment.FRUIT);

        when(alimentRepository.findById(1L)).thenReturn(Optional.of(aliment));
        when(alimentRepository.save(any(Aliment.class))).thenReturn(aliment);

        // When
        Aliment result = alimentService.update(1L, alimentMisAJour);

        // Then
        assertNotNull(result);
        assertEquals("Tomate cerise", result.getNom());
        assertEquals(CategorieAliment.FRUIT, result.getCategorieAliment());
        verify(alimentRepository, times(1)).findById(1L);
        verify(alimentRepository, times(1)).save(aliment);
    }

    @Test
    @DisplayName("update - avec ID inexistant, devrait lancer ResourceNotFoundException")
    void update_avecIdInexistant_devraitLancerException() {
        // Given
        Aliment alimentMisAJour = new Aliment();
        alimentMisAJour.setNom("Inconnu");
        alimentMisAJour.setCategorieAliment(CategorieAliment.LEGUME);

        when(alimentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> alimentService.update(999L, alimentMisAJour)
        );

        assertEquals("Aliment non trouvé avec l'ID: 999", exception.getMessage());
        verify(alimentRepository, times(1)).findById(999L);
        verify(alimentRepository, never()).save(any());
    }

    // ==================== Tests pour deleteById() ====================

    @Test
    @DisplayName("deleteById - avec ID existant, devrait supprimer l'aliment")
    void deleteById_avecIdExistant_devraitSupprimerAliment() {
        // Given
        when(alimentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(alimentRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> alimentService.deleteById(1L));

        // Then
        verify(alimentRepository, times(1)).existsById(1L);
        verify(alimentRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById - avec ID inexistant, devrait lancer ResourceNotFoundException")
    void deleteById_avecIdInexistant_devraitLancerException() {
        // Given
        when(alimentRepository.existsById(999L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> alimentService.deleteById(999L)
        );

        assertEquals("Aliment non trouvé avec l'ID: 999", exception.getMessage());
        verify(alimentRepository, times(1)).existsById(999L);
        verify(alimentRepository, never()).deleteById(any());
    }
}