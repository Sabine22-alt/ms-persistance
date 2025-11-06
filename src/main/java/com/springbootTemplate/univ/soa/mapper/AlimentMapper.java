package com.springbootTemplate.univ.soa.mapper;

import com.springbootTemplate.univ.soa.dto.AlimentDTO;
import com.springbootTemplate.univ.soa.model.Aliment;
import org.springframework.stereotype.Component;

@Component
public class AlimentMapper {

    public AlimentDTO toDTO(Aliment aliment) {
        if (aliment == null) {
            return null;
        }

        AlimentDTO dto = new AlimentDTO();
        dto.setId(aliment.getId());
        dto.setNom(aliment.getNom());
        dto.setCategorie(aliment.getCategorie());

        return dto;
    }

    public Aliment toEntity(AlimentDTO dto) {
        if (dto == null) {
            return null;
        }

        Aliment aliment = new Aliment();
        aliment.setId(dto.getId());
        aliment.setNom(dto.getNom());
        aliment.setCategorie(dto.getCategorie());

        return aliment;
    }
}