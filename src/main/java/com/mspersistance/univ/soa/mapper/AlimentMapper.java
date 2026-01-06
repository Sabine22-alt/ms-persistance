package com.mspersistance.univ.soa.mapper;

import com.mspersistance.univ.soa.dto.AlimentDTO;
import com.mspersistance.univ.soa.model.Aliment;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Aliment et AlimentDTO (Record).
 */
@Component
public class AlimentMapper {

    /**
     * Convertit une entité Aliment en DTO Record
     */
    public AlimentDTO toDTO(Aliment aliment) {
        if (aliment == null) {
            return null;
        }

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

    /**
     * Convertit un DTO Record en entité Aliment
     */
    public Aliment toEntity(AlimentDTO dto) {
        if (dto == null) {
            return null;
        }

        Aliment aliment = new Aliment();
        aliment.setId(dto.id());
        aliment.setNom(dto.nom());
        aliment.setCalories(dto.calories());
        aliment.setProteines(dto.proteines());
        aliment.setGlucides(dto.glucides());
        aliment.setLipides(dto.lipides());
        aliment.setFibres(dto.fibres());
        aliment.setCategorieAliment(dto.categorieAliment());

        return aliment;
    }
}