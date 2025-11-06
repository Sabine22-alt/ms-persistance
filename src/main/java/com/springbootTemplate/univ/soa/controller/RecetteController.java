package com.springbootTemplate.univ.soa.controller;

import com.springbootTemplate.univ.soa.dto.RecetteDTO;
import com.springbootTemplate.univ.soa.mapper.RecetteMapper;
import com.springbootTemplate.univ.soa.model.Recette;
import com.springbootTemplate.univ.soa.service.RecetteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/persistance/recettes")
@CrossOrigin(origins = "*")
public class RecetteController {

    @Autowired
    private RecetteService recetteService;

    @Autowired
    private RecetteMapper recetteMapper;

    /**
     * GET /api/persistance/recettes - Récupérer toutes les recettes
     */
    @GetMapping
    public ResponseEntity<List<RecetteDTO>> getAllRecettes() {
        List<RecetteDTO> dtos = recetteService.findAll().stream()
                .map(recetteMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/recettes/{id} - Récupérer une recette par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecetteDTO> getRecetteById(@PathVariable Long id) {
        return recetteService.findById(id)
                .map(recetteMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/persistance/recettes - Créer une nouvelle recette
     */
    @PostMapping
    public ResponseEntity<?> createRecette(@RequestBody RecetteDTO dto) {
        // Validation : titre requis
        if (dto.getTitre() == null || dto.getTitre().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre de la recette est obligatoire"));
        }

        // Validation : longueur du titre
        if (dto.getTitre().trim().length() < 3) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre doit contenir au moins 3 caractères"));
        }

        if (dto.getTitre().length() > 200) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre ne peut pas dépasser 200 caractères"));
        }

        // Validation : temps total
        if (dto.getTempsTotal() != null && dto.getTempsTotal() <= 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le temps total doit être supérieur à 0"));
        }

        if (dto.getTempsTotal() != null && dto.getTempsTotal() > 1440) { // 24h max
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le temps total ne peut pas dépasser 1440 minutes (24h)"));
        }

        // Validation : kcal
        if (dto.getKcal() != null && dto.getKcal() < 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Les calories ne peuvent pas être négatives"));
        }

        if (dto.getKcal() != null && dto.getKcal() > 10000) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Les calories semblent excessives (max 10000)"));
        }

        // Validation : difficulté
        if (dto.getDifficulte() != null) {
            try {
                Recette.Difficulte.valueOf(dto.getDifficulte().name());
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Difficulté invalide. Valeurs acceptées: FACILE, MOYEN, DIFFICILE"));
            }
        }

        // Validation : ingrédients
        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            for (RecetteDTO.IngredientDTO ingredient : dto.getIngredients()) {
                // Validation : alimentId requis
                if (ingredient.getAlimentId() == null) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ID de l'aliment est requis pour chaque ingrédient"));
                }

                // Validation : quantité
                if (ingredient.getQuantite() != null && ingredient.getQuantite() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("La quantité doit être supérieure à 0"));
                }

                // Validation : unité
                if (ingredient.getUnite() != null && !ingredient.getUnite().isEmpty()) {
                    try {
                        com.springbootTemplate.univ.soa.model.Ingredient.Unite.valueOf(ingredient.getUnite());
                    } catch (Exception e) {
                        return ResponseEntity.badRequest()
                                .body(createErrorResponse("Unité invalide pour un ingrédient. Valeurs acceptées: " +
                                        "GRAMME, KILOGRAMME, LITRE, MILLILITRE, CUILLERE_A_SOUPE, CUILLERE_A_CAFE, SACHET, UNITE"));
                    }
                }
            }
        }

        // Validation : étapes
        if (dto.getEtapes() != null && !dto.getEtapes().isEmpty()) {
            for (RecetteDTO.EtapeDTO etape : dto.getEtapes()) {
                // Validation : ordre requis
                if (etape.getOrdre() == null || etape.getOrdre() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ordre de chaque étape doit être supérieur à 0"));
                }

                // Validation : texte requis
                if (etape.getTexte() == null || etape.getTexte().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le texte de chaque étape est obligatoire"));
                }

                // Validation : longueur texte
                if (etape.getTexte().trim().length() < 5) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le texte de chaque étape doit contenir au moins 5 caractères"));
                }

                // Validation : temps étape
                if (etape.getTemps() != null && etape.getTemps() < 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le temps d'une étape ne peut pas être négatif"));
                }
            }
        }

        try {
            Recette saved = recetteService.saveFromDTO(dto);
            RecetteDTO responseDto = recetteMapper.toDTO(saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la création: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/persistance/recettes/{id} - Mettre à jour une recette
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecette(
            @PathVariable Long id,
            @RequestBody RecetteDTO dto) {

        // Vérifier que la recette existe
        if (recetteService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Recette non trouvée avec l'ID: " + id));
        }

        // Appliquer les mêmes validations que pour la création
        if (dto.getTitre() == null || dto.getTitre().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre de la recette est obligatoire"));
        }

        if (dto.getTitre().trim().length() < 3) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre doit contenir au moins 3 caractères"));
        }

        if (dto.getTitre().length() > 200) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre ne peut pas dépasser 200 caractères"));
        }

        if (dto.getTempsTotal() != null && dto.getTempsTotal() <= 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le temps total doit être supérieur à 0"));
        }

        if (dto.getKcal() != null && dto.getKcal() < 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Les calories ne peuvent pas être négatives"));
        }

        // Validation des ingrédients
        if (dto.getIngredients() != null) {
            for (RecetteDTO.IngredientDTO ingredient : dto.getIngredients()) {
                if (ingredient.getAlimentId() == null) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ID de l'aliment est requis pour chaque ingrédient"));
                }
                if (ingredient.getQuantite() != null && ingredient.getQuantite() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("La quantité doit être supérieure à 0"));
                }
            }
        }

        // Validation des étapes
        if (dto.getEtapes() != null) {
            for (RecetteDTO.EtapeDTO etape : dto.getEtapes()) {
                if (etape.getOrdre() == null || etape.getOrdre() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ordre de chaque étape doit être supérieur à 0"));
                }
                if (etape.getTexte() == null || etape.getTexte().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le texte de chaque étape est obligatoire"));
                }
            }
        }

        try {
            Recette updated = recetteService.updateFromDTO(id, dto);
            RecetteDTO responseDto = recetteMapper.toDTO(updated);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/persistance/recettes/{id} - Supprimer une recette
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecette(@PathVariable Long id) {
        // Vérifier que la recette existe
        if (recetteService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Recette non trouvée avec l'ID: " + id));
        }

        try {
            recetteService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    // Méthode utilitaire
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}