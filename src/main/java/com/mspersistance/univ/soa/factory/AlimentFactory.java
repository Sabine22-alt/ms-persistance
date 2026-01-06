package com.mspersistance.univ.soa.factory;

import com.mspersistance.univ.soa.dto.AlimentDTO;
import com.mspersistance.univ.soa.model.Aliment;
import org.springframework.stereotype.Component;

/**
 * Factory pour créer des entités Aliment.
 * Design Pattern: Factory Method + Builder
 */
@Component
public class AlimentFactory {

    /**
     * Crée un Aliment depuis un DTO Record
     */
    public Aliment createFromDTO(AlimentDTO dto) {
        return Aliment.builder()
                .nom(dto.nom())
                .calories(dto.calories())
                .proteines(dto.proteines())
                .glucides(dto.glucides())
                .lipides(dto.lipides())
                .fibres(dto.fibres())
                .categorieAliment(dto.categorieAliment())
                .build();
    }

    /**
     * Met à jour un Aliment existant avec les données du DTO
     */
    public Aliment updateFromDTO(Aliment existing, AlimentDTO dto) {
        if (dto.nom() != null) {
            existing.setNom(dto.nom());
        }
        existing.setCalories(dto.calories());
        existing.setProteines(dto.proteines());
        existing.setGlucides(dto.glucides());
        existing.setLipides(dto.lipides());
        existing.setFibres(dto.fibres());
        if (dto.categorieAliment() != null) {
            existing.setCategorieAliment(dto.categorieAliment());
        }
        return existing;
    }

    /**
     * Convertit une entité en DTO Record
     */
    public AlimentDTO toDTO(Aliment aliment) {
        return new AlimentDTO(
                aliment.getId(),
                aliment.getNom(),
                aliment.getCalories(),
                aliment.getProteines(),
                aliment.getGlucides(),
                aliment.getLipides(),
                aliment.getFibres(),
                aliment.getCategorieAliment()
        );
    }
}

