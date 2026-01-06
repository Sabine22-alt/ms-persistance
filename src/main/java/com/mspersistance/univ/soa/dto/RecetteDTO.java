package com.mspersistance.univ.soa.dto;

import com.mspersistance.univ.soa.model.Recette;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO Recette en Record - Immutable et performant
 * Avec nested records pour Ingredients et Etapes
 */
public record RecetteDTO(
    Long id,
    String titre,
    String description,
    Integer tempsTotal,
    Integer kcal,
    String imageUrl,
    Recette.Difficulte difficulte,
    LocalDateTime dateCreation,
    LocalDateTime dateModification,
    Boolean actif,
    Recette.StatutRecette statut,
    String motifRejet,
    Long utilisateurId,
    Double moyenneEvaluation,
    List<IngredientDTO> ingredients,
    List<EtapeDTO> etapes
) {
    /**
     * DTO Ingredient imbriqué en Record
     */
    public record IngredientDTO(
        Long id,
        Long alimentId,
        String alimentNom,
        String nomAliment,
        Float quantite,
        String unite,
        Boolean principal
    ) {}

    /**
     * DTO Etape imbriqué en Record
     */
    public record EtapeDTO(
        Long id,
        Integer ordre,
        Integer temps,
        String texte
    ) {}
}