package com.mspersistance.univ.soa.factory;

import com.mspersistance.univ.soa.dto.RecetteDTO;
import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.model.Aliment;
import com.mspersistance.univ.soa.model.Etape;
import com.mspersistance.univ.soa.model.Ingredient;
import com.mspersistance.univ.soa.model.Recette;
import com.mspersistance.univ.soa.repository.AlimentRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Factory pour crÃ©er des entitÃ©s Recette.
 * Design Pattern: Factory Method + Builder
 *
 * GÃ¨re la complexitÃ© de crÃ©ation des Recettes avec:
 * - IngrÃ©dients imbriquÃ©s
 * - Ã‰tapes imbriquÃ©es
 * - CrÃ©ation automatique d'aliments
 */
@Component
public class RecetteFactory {

    private final AlimentRepository alimentRepository;

    public RecetteFactory(AlimentRepository alimentRepository) {
        this.alimentRepository = alimentRepository;
    }

    /**
     * CrÃ©e une Recette depuis un DTO avec gestion des ingrÃ©dients/Ã©tapes
     */
    public Recette createFromDTO(RecetteDTO dto) {
        // Validation
        if (dto.ingredients() == null || dto.ingredients().isEmpty()) {
            throw new IllegalArgumentException("Au moins un ingrÃ©dient est requis");
        }

        // CrÃ©ation recette avec Builder
        Recette recette = Recette.builder()
                .titre(dto.titre())
                .description(dto.description())
                .tempsTotal(dto.tempsTotal())
                .kcal(dto.kcal())
                .imageUrl(dto.imageUrl())
                .difficulte(dto.difficulte())
                .actif(Boolean.FALSE)  // Par dÃ©faut inactif
                .statut(Recette.StatutRecette.EN_ATTENTE)  // En attente validation
                .utilisateurId(dto.utilisateurId())
                .ingredients(new ArrayList<>())
                .etapes(new ArrayList<>())
                .build();

        // Traiter les ingrÃ©dients
        for (RecetteDTO.IngredientDTO ingredientDTO : dto.ingredients()) {
            Ingredient ingredient = createIngredient(ingredientDTO, recette);
            recette.getIngredients().add(ingredient);
        }

        // Traiter les Ã©tapes
        if (dto.etapes() != null && !dto.etapes().isEmpty()) {
            for (RecetteDTO.EtapeDTO etapeDTO : dto.etapes()) {
                Etape etape = createEtape(etapeDTO, recette);
                recette.getEtapes().add(etape);
            }
        }

        return recette;
    }

    /**
     * Met Ã  jour une Recette existante depuis un DTO
     */
    public Recette updateFromDTO(Recette existing, RecetteDTO dto) {
        // Mise Ã  jour champs de base
        existing.setTitre(dto.titre());
        existing.setDescription(dto.description());
        existing.setTempsTotal(dto.tempsTotal());
        existing.setKcal(dto.kcal());
        existing.setImageUrl(dto.imageUrl());
        existing.setDifficulte(dto.difficulte());

        // Mise Ã  jour ingrÃ©dients
        existing.getIngredients().clear();
        if (dto.ingredients() != null && !dto.ingredients().isEmpty()) {
            for (RecetteDTO.IngredientDTO ingredientDTO : dto.ingredients()) {
                Ingredient ingredient = createIngredient(ingredientDTO, existing);
                existing.getIngredients().add(ingredient);
            }
        }

        // Mise Ã  jour Ã©tapes
        existing.getEtapes().clear();
        if (dto.etapes() != null && !dto.etapes().isEmpty()) {
            for (RecetteDTO.EtapeDTO etapeDTO : dto.etapes()) {
                Etape etape = createEtape(etapeDTO, existing);
                existing.getEtapes().add(etape);
            }
        }

        return existing;
    }

    /**
     * CrÃ©e un Ingredient depuis un DTO
     * GÃ¨re la crÃ©ation automatique d'aliments
     */
    private Ingredient createIngredient(RecetteDTO.IngredientDTO dto, Recette recette) {
        // Extraction du nom d'aliment
        String nomAliment = extractNomAliment(dto);

        // Validation
        if (nomAliment == null && dto.alimentId() == null) {
            throw new IllegalArgumentException("L'ID ou le nom de l'aliment est requis");
        }

        // CrÃ©ation de l'ingrÃ©dient avec Builder
        Ingredient.IngredientBuilder builder = Ingredient.builder()
                .quantite(dto.quantite())
                .unite(dto.unite() != null ? Ingredient.Unite.valueOf(dto.unite()) : null)
                .principal(dto.principal())
                .nomAliment(nomAliment)
                .recette(recette);

        // RÃ©solution de l'aliment
        Aliment aliment = resolveAliment(nomAliment, dto.alimentId());
        builder.aliment(aliment);

        return builder.build();
    }

    /**
     * CrÃ©e une Etape depuis un DTO
     */
    private Etape createEtape(RecetteDTO.EtapeDTO dto, Recette recette) {
        return Etape.builder()
                .ordre(dto.ordre())
                .temps(dto.temps())
                .texte(dto.texte())
                .recette(recette)
                .build();
    }

    /**
     * Extrait le nom d'aliment depuis le DTO (gÃ¨re plusieurs champs)
     */
    private String extractNomAliment(RecetteDTO.IngredientDTO dto) {
        if (dto.alimentNom() != null && !dto.alimentNom().trim().isEmpty()) {
            return dto.alimentNom().trim();
        }
        if (dto.nomAliment() != null && !dto.nomAliment().trim().isEmpty()) {
            return dto.nomAliment().trim();
        }
        return null;
    }

    /**
     * RÃ©sout un Aliment par nom ou ID
     * CrÃ©e automatiquement l'aliment s'il n'existe pas
     * PrioritÃ© Ã  l'ID pour Ã©viter les ambiguÃ¯tÃ©s
     */
    private Aliment resolveAliment(String nomAliment, Long alimentId) {
        // PrioritÃ© Ã  l'ID pour Ã©viter ambiguÃ¯tÃ©
        if (alimentId != null) {
            return alimentRepository.findById(alimentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Aliment non trouvÃ© avec l'ID: " + alimentId));
        }

        // Sinon utiliser le nom
        if (nomAliment != null) {
            return findOrCreateAliment(nomAliment);
        }

        throw new IllegalArgumentException("Nom ou ID d'aliment requis");
    }

    /**
     * Trouve ou crÃ©e un aliment par nom
     */
    private Aliment findOrCreateAliment(String nom) {
        Optional<Aliment> existant = alimentRepository.findByNomIgnoreCase(nom);

        if (existant.isPresent()) {
            return existant.get();
        }

        // CrÃ©er un nouvel aliment avec Builder
        Aliment nouvelAliment = Aliment.builder()
                .nom(nom)
                .calories(0f)
                .proteines(0f)
                .glucides(0f)
                .lipides(0f)
                .fibres(0f)
                .categorieAliment(Aliment.CategorieAliment.AUTRE)
                .build();

        return alimentRepository.save(nouvelAliment);
    }
}

