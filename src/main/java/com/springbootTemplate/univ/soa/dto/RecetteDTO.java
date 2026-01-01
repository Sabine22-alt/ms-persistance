package com.springbootTemplate.univ.soa.dto;

import com.springbootTemplate.univ.soa.model.Recette;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecetteDTO {
    private Long id;
    private String titre;
    private String description;
    private Integer tempsTotal;
    private Integer kcal;
    private String imageUrl;
    private Recette.Difficulte difficulte;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private Boolean actif;
    private Recette.StatutRecette statut;
    private String motifRejet;
    private Long utilisateurId;
    private Double moyenneEvaluation; // Moyenne des notes
    private List<IngredientDTO> ingredients;
    private List<EtapeDTO> etapes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDTO {
        private Long id;
        private Long alimentId;
        private String alimentNom;  // Nom de l'aliment (depuis référence)
        private String nomAliment;  // Nom libre de l'aliment (si pas de référence)
        private Float quantite;
        private String unite;
        private Boolean principal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EtapeDTO {
        private Long id;
        private Integer ordre;
        private Integer temps;
        private String texte;
    }
}