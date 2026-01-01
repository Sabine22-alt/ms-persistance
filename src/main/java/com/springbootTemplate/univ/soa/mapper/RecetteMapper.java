package com.springbootTemplate.univ.soa.mapper;

import com.springbootTemplate.univ.soa.dto.RecetteDTO;
import com.springbootTemplate.univ.soa.model.Etape;
import com.springbootTemplate.univ.soa.model.Ingredient;
import com.springbootTemplate.univ.soa.model.Recette;
import com.springbootTemplate.univ.soa.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RecetteMapper {

    @Autowired
    private MinioService minioService;

    public RecetteDTO toDTO(Recette recette) {
        if (recette == null) {
            return null;
        }

        RecetteDTO dto = new RecetteDTO();
        dto.setId(recette.getId());
        dto.setTitre(recette.getTitre());
        dto.setDescription(recette.getDescription());
        dto.setTempsTotal(recette.getTempsTotal());
        dto.setKcal(recette.getKcal());

        if (recette.getImageUrl() != null && !recette.getImageUrl().isEmpty()) {
            if (recette.getImageUrl().startsWith("http://") || recette.getImageUrl().startsWith("https://")) {
                dto.setImageUrl(recette.getImageUrl());
            } else {
                try {
                    String presignedUrl = minioService.getPresignedUrl(
                        minioService.getRecettesBucket(),
                        recette.getImageUrl()
                    );
                    dto.setImageUrl(presignedUrl);
                } catch (Exception e) {
                    dto.setImageUrl(recette.getImageUrl());
                }
            }
        } else {
            dto.setImageUrl(null);
        }

        dto.setDifficulte(recette.getDifficulte());
        dto.setDateCreation(recette.getDateCreation());
        dto.setDateModification(recette.getDateModification());
        dto.setActif(recette.getActif());
        dto.setStatut(recette.getStatut());
        dto.setMotifRejet(recette.getMotifRejet());
        dto.setUtilisateurId(recette.getUtilisateurId());
        dto.setMoyenneEvaluation(recette.getMoyenneEvaluation());

        if (recette.getIngredients() != null) {
            dto.setIngredients(
                    recette.getIngredients().stream()
                            .map(this::ingredientToDTO)
                            .collect(Collectors.toList())
            );
        }

        if (recette.getEtapes() != null) {
            dto.setEtapes(
                    recette.getEtapes().stream()
                            .map(this::etapeToDTO)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    private RecetteDTO.IngredientDTO ingredientToDTO(Ingredient ingredient) {
        RecetteDTO.IngredientDTO dto = new RecetteDTO.IngredientDTO();
        dto.setId(ingredient.getId());

        // Si l'aliment est lié, utiliser ses données
        if (ingredient.getAliment() != null) {
            dto.setAlimentId(ingredient.getAliment().getId());
            dto.setAlimentNom(ingredient.getAliment().getNom());
        } else if (ingredient.getNomAliment() != null && !ingredient.getNomAliment().trim().isEmpty()) {
            dto.setAlimentNom(ingredient.getNomAliment());
        }
        dto.setNomAliment(ingredient.getNomAliment());

        dto.setQuantite(ingredient.getQuantite());
        dto.setUnite(ingredient.getUnite() != null ? ingredient.getUnite().name() : null);
        dto.setPrincipal(ingredient.getPrincipal());
        return dto;
    }

    private RecetteDTO.EtapeDTO etapeToDTO(Etape etape) {
        RecetteDTO.EtapeDTO dto = new RecetteDTO.EtapeDTO();
        dto.setId(etape.getId());
        dto.setOrdre(etape.getOrdre());
        dto.setTemps(etape.getTemps());
        dto.setTexte(etape.getTexte());
        return dto;
    }

    public Recette toEntity(RecetteDTO dto) {
        if (dto == null) {
            return null;
        }

        Recette recette = new Recette();
        recette.setId(dto.getId());
        recette.setTitre(dto.getTitre());
        recette.setDescription(dto.getDescription());
        recette.setTempsTotal(dto.getTempsTotal());
        recette.setKcal(dto.getKcal());
        recette.setImageUrl(dto.getImageUrl());
        recette.setDifficulte(dto.getDifficulte());
        recette.setActif(dto.getActif());
        recette.setStatut(dto.getStatut());
        recette.setMotifRejet(dto.getMotifRejet());
        return recette;
    }
}