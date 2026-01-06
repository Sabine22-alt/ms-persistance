package com.mspersistance.univ.soa.mapper;

import com.mspersistance.univ.soa.dto.RecetteDTO;
import com.mspersistance.univ.soa.model.Etape;
import com.mspersistance.univ.soa.model.Ingredient;
import com.mspersistance.univ.soa.model.Recette;
import com.mspersistance.univ.soa.service.MinioService;
import org.springframework.stereotype.Component;


/**
 * Mapper pour convertir entre Recette et RecetteDTO (Record).
 * Utilise les constructeurs complets des Records au lieu de setters.
 */
@Component
public class RecetteMapper {

    private final MinioService minioService;

    public RecetteMapper(MinioService minioService) {
        this.minioService = minioService;
    }

    /**
     * Convertit une entité Recette en DTO Record (VERSION OPTIMISÉE)
     * N'accède aux collections lazy QUE si déjà initialisées (évite N+1)
     */
    public RecetteDTO toDTO(Recette recette) {
        if (recette == null) {
            return null;
        }

        // Gérer l'URL de l'image (MinIO presigned URL)
        String imageUrl = resolveImageUrl(recette.getImageUrl());

        // Convertir les ingrédients SEULEMENT si la collection est initialisée ET non-null
        var ingredients = (recette.getIngredients() != null && isCollectionInitialized(recette.getIngredients()))
                ? recette.getIngredients().stream()
                        .map(this::ingredientToDTO)
                        .toList()
                : null;

        // Convertir les étapes SEULEMENT si la collection est initialisée ET non-null
        var etapes = (recette.getEtapes() != null && isCollectionInitialized(recette.getEtapes()))
                ? recette.getEtapes().stream()
                        .map(this::etapeToDTO)
                        .toList()
                : null;

        // Constructeur Record complet
        return new RecetteDTO(
                recette.getId(),
                recette.getTitre(),
                recette.getDescription(),
                recette.getTempsTotal(),
                recette.getKcal(),
                imageUrl,
                recette.getDifficulte(),
                recette.getDateCreation(),
                recette.getDateModification(),
                recette.getActif(),
                recette.getStatut(),
                recette.getMotifRejet(),
                recette.getUtilisateurId(),
                recette.getMoyenneEvaluation(),
                ingredients,
                etapes
        );
    }

    /**
     * Version légère pour listes (pas de collections)
     */
    public RecetteDTO toDTOLight(Recette recette) {
        if (recette == null) {
            return null;
        }

        String imageUrl = resolveImageUrl(recette.getImageUrl());

        return new RecetteDTO(
                recette.getId(),
                recette.getTitre(),
                recette.getDescription(),
                recette.getTempsTotal(),
                recette.getKcal(),
                imageUrl,
                recette.getDifficulte(),
                recette.getDateCreation(),
                recette.getDateModification(),
                recette.getActif(),
                recette.getStatut(),
                recette.getMotifRejet(),
                recette.getUtilisateurId(),
                recette.getMoyenneEvaluation(),
                null, // pas d'ingredients
                null  // pas d'etapes
        );
    }

    /**
     * Vérifie si une collection Hibernate est déjà initialisée (évite le lazy loading)
     */
    private boolean isCollectionInitialized(Object collection) {
        return org.hibernate.Hibernate.isInitialized(collection);
    }

    /**
     * Convertit un Ingredient en IngredientDTO Record
     */
    private RecetteDTO.IngredientDTO ingredientToDTO(Ingredient ingredient) {
        Long alimentId = ingredient.getAliment() != null ? ingredient.getAliment().getId() : null;
        String alimentNom = ingredient.getAliment() != null
                ? ingredient.getAliment().getNom()
                : ingredient.getNomAliment();
        String unite = ingredient.getUnite() != null ? ingredient.getUnite().name() : null;

        return new RecetteDTO.IngredientDTO(
                ingredient.getId(),
                alimentId,
                alimentNom,
                ingredient.getNomAliment(),
                ingredient.getQuantite(),
                unite,
                ingredient.getPrincipal()
        );
    }

    /**
     * Convertit une Etape en EtapeDTO Record
     */
    private RecetteDTO.EtapeDTO etapeToDTO(Etape etape) {
        return new RecetteDTO.EtapeDTO(
                etape.getId(),
                etape.getOrdre(),
                etape.getTemps(),
                etape.getTexte()
        );
    }

    /**
     * Résout l'URL de l'image (génère une presigned URL pour MinIO)
     */
    private String resolveImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }

        try {
            return minioService.getPresignedUrl(minioService.getRecettesBucket(), imageUrl);
        } catch (Exception e) {
            return imageUrl;
        }
    }

    /**
     * Convertit un DTO Record en entité Recette (partiel)
     */
    public Recette toEntity(RecetteDTO dto) {
        if (dto == null) {
            return null;
        }

        Recette recette = new Recette();
        recette.setId(dto.id());
        recette.setTitre(dto.titre());
        recette.setDescription(dto.description());
        recette.setTempsTotal(dto.tempsTotal());
        recette.setKcal(dto.kcal());
        recette.setImageUrl(dto.imageUrl());
        recette.setDifficulte(dto.difficulte());
        recette.setActif(dto.actif());
        recette.setStatut(dto.statut());
        recette.setMotifRejet(dto.motifRejet());
        recette.setUtilisateurId(dto.utilisateurId());

        return recette;
    }
}