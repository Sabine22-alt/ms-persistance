package com.springbootTemplate.univ.soa.mapper;

import com.springbootTemplate.univ.soa.dto.ActiviteDTO;
import com.springbootTemplate.univ.soa.model.Activite;
import org.springframework.stereotype.Component;

@Component
public class ActiviteMapper {

    public ActiviteDTO toDTO(Activite activite) {
        if (activite == null) {
            return null;
        }

        ActiviteDTO dto = new ActiviteDTO();
        dto.setId(activite.getId());
        dto.setUtilisateurId(activite.getUtilisateurId());
        dto.setType(activite.getType());
        dto.setDescription(activite.getDescription());
        dto.setDateActivite(activite.getDateActivite());
        dto.setDetails(activite.getDetails());

        return dto;
    }

    public Activite toEntity(ActiviteDTO dto) {
        if (dto == null) {
            return null;
        }

        Activite activite = new Activite();
        activite.setId(dto.getId());
        activite.setUtilisateurId(dto.getUtilisateurId());
        activite.setType(dto.getType());
        activite.setDescription(dto.getDescription());
        activite.setDateActivite(dto.getDateActivite());
        activite.setDetails(dto.getDetails());

        return activite;
    }
}

