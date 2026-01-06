package com.mspersistance.univ.soa.mapper;

import com.mspersistance.univ.soa.dto.PlanificationRepasDTO;
import com.mspersistance.univ.soa.model.PlanificationRepas;
import com.mspersistance.univ.soa.model.PlanificationJour;
import com.mspersistance.univ.soa.model.RepasPlannifie;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PlanificationRepasMapper {

    public PlanificationRepasDTO toDTO(PlanificationRepas planification) {
        if (planification == null) {
            return null;
        }

        PlanificationRepasDTO dto = new PlanificationRepasDTO();
        dto.setId(planification.getId());
        dto.setUtilisateurId(planification.getUtilisateurId());
        dto.setSemaine(planification.getSemaine());
        dto.setAnnee(planification.getAnnee());
        dto.setDateCreation(planification.getDateCreation());
        dto.setDateModification(planification.getDateModification());

        if (planification.getJours() != null) {
            dto.setJours(planification.getJours().stream()
                .map(this::jjourToDTO)
                .collect(Collectors.toList())
            );
        }

        return dto;
    }

    private PlanificationRepasDTO.PlanificationJourDTO jjourToDTO(PlanificationJour jour) {
        PlanificationRepasDTO.PlanificationJourDTO dto = new PlanificationRepasDTO.PlanificationJourDTO();
        dto.setId(jour.getId());
        dto.setJour(jour.getJour());

        if (jour.getRepas() != null) {
            dto.setRepas(jour.getRepas().stream()
                .map(this::repasToDTO)
                .collect(Collectors.toList())
            );
        }

        return dto;
    }

    private PlanificationRepasDTO.RepasPlannifieDTO repasToDTO(RepasPlannifie repas) {
        PlanificationRepasDTO.RepasPlannifieDTO dto = new PlanificationRepasDTO.RepasPlannifieDTO();
        dto.setId(repas.getId());
        dto.setTypeRepas(repas.getTypeRepas().getValue());
        dto.setRecetteId(repas.getRecetteId());
        dto.setNoteLibre(repas.getNoteLibre());

        return dto;
    }
}

