package com.mspersistance.univ.soa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanificationRepasDTO {
    private Long id;
    private Long utilisateurId;
    private Integer semaine;
    private Integer annee;
    private List<PlanificationJourDTO> jours;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanificationJourDTO {
        private Long id;
        private Integer jour; // 0=lundi, 6=dimanche
        private List<RepasPlannifieDTO> repas;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepasPlannifieDTO {
        private Long id;
        private Integer typeRepas; // 0=petit-dÃ©j, 1=dÃ©jeuner, 2=dÃ®ner
        private Long recetteId;
        private String recetteTitre; // Titre de la recette si liÃ©e
        private String noteLibre;
    }
}

