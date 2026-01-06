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
 * Factory pour créer des entités Recette.
 * Design Pattern: Factory Method + Builder
 *
 * Gère la complexité de création des Recettes avec:
 * - Ingrédients imbriqués
 * - Étapes imbriquées
 * - Création automatique d'aliments
 */
@Component
public class RecetteFactory {

    private final AlimentRepository alimentRepository;

    public RecetteFactory(AlimentRepository alimentRepository) {
        this.alimentRepository = alimentRepository;
    }

    /**
     * Crée une Recette depuis un DTO avec gestion des ingrédients/étapes
     */
    public Recette createFromDTO(RecetteDTO dto) {
        // Validation
        if (dto.ingredients() == null || dto.ingredients().isEmpty()) {
            throw new IllegalArgumentException("Au moins un ingrédient est requis");
        }

        // Création recette avec Builder
        Recette recette = Recette.builder()
                .titre(dto.titre())
                .description(dto.description())
                .tempsTotal(dto.tempsTotal())
                .kcal(dto.kcal())
                .imageUrl(dto.imageUrl())
                .difficulte(dto.difficulte())
                .actif(Boolean.FALSE)  // Par défaut inactif
                .statut(Recette.StatutRecette.EN_ATTENTE)  // En attente validation
                .utilisateurId(dto.utilisateurId())
                .ingredients(new ArrayList<>())
                .etapes(new ArrayList<>())
                .build();

        // Traiter les ingrédients
        for (RecetteDTO.IngredientDTO ingredientDTO : dto.ingredients()) {
            Ingredient ingredient = createIngredient(ingredientDTO, recette);
            recette.getIngredients().add(ingredient);
        }

        // Traiter les étapes
        if (dto.etapes() != null && !dto.etapes().isEmpty()) {
            for (RecetteDTO.EtapeDTO etapeDTO : dto.etapes()) {
                Etape etape = createEtape(etapeDTO, recette);
                recette.getEtapes().add(etape);
            }
        }

        return recette;
    }

    /**
     * Met à jour une Recette existante depuis un DTO
     */
    public Recette updateFromDTO(Recette existing, RecetteDTO dto) {
        // Mise à jour champs de base
        existing.setTitre(dto.titre());
        existing.setDescription(dto.description());
        existing.setTempsTotal(dto.tempsTotal());
        existing.setKcal(dto.kcal());
        existing.setImageUrl(dto.imageUrl());
        existing.setDifficulte(dto.difficulte());

        // Mise à jour ingrédients
        existing.getIngredients().clear();
        if (dto.ingredients() != null && !dto.ingredients().isEmpty()) {
            for (RecetteDTO.IngredientDTO ingredientDTO : dto.ingredients()) {
                Ingredient ingredient = createIngredient(ingredientDTO, existing);
                existing.getIngredients().add(ingredient);
            }
        }

        // Mise à jour étapes
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
     * Crée un Ingredient depuis un DTO
     * Gère la création automatique d'aliments
     */
    private Ingredient createIngredient(RecetteDTO.IngredientDTO dto, Recette recette) {
        // Extraction du nom d'aliment
        String nomAliment = extractNomAliment(dto);

        // Validation
        if (nomAliment == null && dto.alimentId() == null) {
            throw new IllegalArgumentException("L'ID ou le nom de l'aliment est requis");
        }

        // Création de l'ingrédient avec Builder
        Ingredient.IngredientBuilder builder = Ingredient.builder()
                .quantite(dto.quantite())
                .unite(dto.unite() != null ? Ingredient.Unite.valueOf(dto.unite()) : null)
                .principal(dto.principal())
                .nomAliment(nomAliment)
                .recette(recette);

        // Résolution de l'aliment
        Aliment aliment = resolveAliment(nomAliment, dto.alimentId());
        builder.aliment(aliment);

        return builder.build();
    }

    /**
     * Crée une Etape depuis un DTO
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
     * Extrait le nom d'aliment depuis le DTO (gère plusieurs champs)
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
     * Résout un Aliment par nom ou ID
     * Crée automatiquement l'aliment s'il n'existe pas
     * Priorité à l'ID pour éviter les ambiguïtés
     */
    private Aliment resolveAliment(String nomAliment, Long alimentId) {
        // Priorité à l'ID pour éviter ambiguïté
        if (alimentId != null) {
            return alimentRepository.findById(alimentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Aliment non trouvé avec l'ID: " + alimentId));
        }

        // Sinon utiliser le nom
        if (nomAliment != null) {
            return findOrCreateAliment(nomAliment);
        }

        throw new IllegalArgumentException("Nom ou ID d'aliment requis");
    }

    /**
     * Trouve ou crée un aliment par nom
     */
    private Aliment findOrCreateAliment(String nom) {
        Optional<Aliment> existant = alimentRepository.findByNomIgnoreCase(nom);

        if (existant.isPresent()) {
            return existant.get();
        }

        // Créer un nouvel aliment avec Builder
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

