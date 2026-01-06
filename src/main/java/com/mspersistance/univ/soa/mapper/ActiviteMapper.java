package com.mspersistance.univ.soa.mapper;

import com.mspersistance.univ.soa.dto.ActiviteDTO;
import com.mspersistance.univ.soa.model.Activite;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Activite et ActiviteDTO (Record).
 */
@Component
public class ActiviteMapper {

    /**
     * Convertit une entité Activite en DTO Record
     */
    public ActiviteDTO toDTO(Activite activite) {
        if (activite == null) {
            return null;
        }

        return new ActiviteDTO(
                activite.getId(),
                activite.getUtilisateurId(),
                activite.getType(),
                activite.getDescription(),
                activite.getDateActivite(),
                activite.getDetails()
        );
    }

    /**
     * Convertit un DTO Record en entité Activite
     */
    public Activite toEntity(ActiviteDTO dto) {
        if (dto == null) {
            return null;
        }

        Activite activite = new Activite();
        activite.setId(dto.id());
        activite.setUtilisateurId(dto.utilisateurId());
        activite.setType(dto.type());
        activite.setDescription(dto.description());
        activite.setDateActivite(dto.dateActivite());
        activite.setDetails(dto.details());

        return activite;
    }
}

