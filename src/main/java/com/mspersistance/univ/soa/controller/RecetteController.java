package com.mspersistance.univ.soa.controller;

import com.mspersistance.univ.soa.model.Ingredient;
import com.mspersistance.univ.soa.dto.RecetteDTO;
import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.mapper.RecetteMapper;
import com.mspersistance.univ.soa.model.Recette;
import com.mspersistance.univ.soa.model.Recette.StatutRecette;
import com.mspersistance.univ.soa.service.RecetteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/persistance/recettes")
@CrossOrigin(origins = "*")
public class RecetteController {

    private final RecetteService recetteService;
    private final RecetteMapper recetteMapper;

    @Autowired
    public RecetteController(RecetteService recetteService, RecetteMapper recetteMapper) {
        this.recetteService = recetteService;
        this.recetteMapper = recetteMapper;
    }

    /**
     * GET /api/persistance/recettes - Récupérer toutes les recettes (VERSION OPTIMISÉE)
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<RecetteDTO>> getAllRecettes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        // Validation pagination
        if (size > 100) size = 100;
        if (page < 0) page = 0;

        List<RecetteDTO> dtos = recetteService.findAll().stream()
                .skip((long) page * size)
                .limit(size)
                .map(recetteMapper::toDTOLight) // VERSION LÉGÈRE sans collections
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/recettes/en-attente - Récupérer recettes en attente de validation
     */
    @GetMapping("/en-attente")
    @Transactional(readOnly = true)
    public ResponseEntity<List<RecetteDTO>> getRecettesEnAttente() {
        List<RecetteDTO> dtos = recetteService.findByStatut(StatutRecette.EN_ATTENTE).stream()
                .map(recetteMapper::toDTOLight) // VERSION LÉGÈRE
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/recettes/utilisateur/{utilisateurId} - Récupérer les recettes d'un utilisateur
     */
    @GetMapping("/utilisateur/{utilisateurId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<RecetteDTO>> getRecettesByUtilisateur(@PathVariable Long utilisateurId) {
        List<RecetteDTO> dtos = recetteService.findByUtilisateurId(utilisateurId).stream()
                .map(recetteMapper::toDTOLight) // VERSION LÉGÈRE
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/recettes/{id} - Récupérer une recette par ID (VERSION COMPLÈTE)
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<RecetteDTO> getRecetteById(@PathVariable Long id) {
        return recetteService.findById(id)
                .map(recetteMapper::toDTO) // VERSION COMPLÈTE avec collections
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/persistance/recettes - Créer une nouvelle recette
     */
    @PostMapping
    public ResponseEntity<?> createRecette(@RequestBody RecetteDTO dto) {
        // Validation : titre requis
        if (dto.titre() == null || dto.titre().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre de la recette est obligatoire"));
        }

        // Validation : longueur du titre
        if (dto.titre().trim().length() < 3) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre doit contenir au moins 3 caractères"));
        }

        if (dto.titre().length() > 200) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre ne peut pas dépasser 200 caractères"));
        }

        // Validation : temps total
        if (dto.tempsTotal() != null && dto.tempsTotal() <= 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le temps total doit être supérieur à 0"));
        }

        if (dto.tempsTotal() != null && dto.tempsTotal() > 1440) { // 24h max
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le temps total ne peut pas dépasser 1440 minutes (24h)"));
        }

        // Validation : kcal
        if (dto.kcal() != null && dto.kcal() < 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Les calories ne peuvent pas être négatives"));
        }

        if (dto.kcal() != null && dto.kcal() > 10000) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Les calories semblent excessives (max 10000)"));
        }

        // Validation : difficulté
        if (dto.difficulte() != null) {
            try {
                Recette.Difficulte.valueOf(dto.difficulte().name());
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Difficulté invalide. Valeurs acceptées: FACILE, MOYEN, DIFFICILE"));
            }
        }

        // Validation : ingrédients
        if (dto.ingredients() != null && !dto.ingredients().isEmpty()) {
            for (RecetteDTO.IngredientDTO ingredient : dto.ingredients()) {
                // Validation : alimentId OU alimentNom requis
                if (ingredient.alimentId() == null &&
                    (ingredient.alimentNom() == null || ingredient.alimentNom().trim().isEmpty())) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ID ou le nom de l'aliment est requis pour chaque ingrédient"));
                }

                // Validation : quantité
                if (ingredient.quantite() != null && ingredient.quantite() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("La quantité doit être supérieure à 0"));
                }

                // Validation : unité
                if (ingredient.unite() != null && !ingredient.unite().isEmpty()) {
                    try {
                        Ingredient.Unite.valueOf(ingredient.unite());
                    } catch (Exception e) {
                        return ResponseEntity.badRequest()
                                .body(createErrorResponse("Unité invalide pour un ingrédient. Valeurs acceptées: " +
                                        "GRAMME, KILOGRAMME, LITRE, MILLILITRE, CUILLERE_A_SOUPE, CUILLERE_A_CAFE, SACHET, UNITE"));
                    }
                }
            }
        }

        // Validation : étapes
        if (dto.etapes() != null && !dto.etapes().isEmpty()) {
            for (RecetteDTO.EtapeDTO etape : dto.etapes()) {
                // Validation : ordre requis
                if (etape.ordre() == null || etape.ordre() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ordre de chaque étape doit être supérieur à 0"));
                }

                // Validation : texte requis
                if (etape.texte() == null || etape.texte().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le texte de chaque étape est obligatoire"));
                }

                // Validation : longueur texte
                if (etape.texte().trim().length() < 5) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le texte de chaque étape doit contenir au moins 5 caractères"));
                }

                // Validation : temps étape
                if (etape.temps() != null && etape.temps() < 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le temps d'une étape ne peut pas être négatif"));
                }
            }
        }

        try {
            Recette saved = recetteService.saveFromDTO(dto);
            RecetteDTO responseDto = recetteMapper.toDTO(saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
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
        if (dto.titre() == null || dto.titre().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre de la recette est obligatoire"));
        }

        if (dto.titre().trim().length() < 3) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre doit contenir au moins 3 caractères"));
        }

        if (dto.titre().length() > 200) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre ne peut pas dépasser 200 caractères"));
        }

        if (dto.tempsTotal() != null && dto.tempsTotal() <= 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le temps total doit être supérieur à 0"));
        }

        if (dto.kcal() != null && dto.kcal() < 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Les calories ne peuvent pas être négatives"));
        }

        // Validation des ingrédients
        if (dto.ingredients() != null) {
            for (RecetteDTO.IngredientDTO ingredient : dto.ingredients()) {
                // Validation : alimentId OU alimentNom requis
                if (ingredient.alimentId() == null &&
                    (ingredient.alimentNom() == null || ingredient.alimentNom().trim().isEmpty())) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ID ou le nom de l'aliment est requis pour chaque ingrédient"));
                }
                if (ingredient.quantite() != null && ingredient.quantite() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("La quantité doit être supérieure à 0"));
                }
            }
        }

        // Validation des étapes
        if (dto.etapes() != null) {
            for (RecetteDTO.EtapeDTO etape : dto.etapes()) {
                if (etape.ordre() == null || etape.ordre() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ordre de chaque étape doit être supérieur à 0"));
                }
                if (etape.texte() == null || etape.texte().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le texte de chaque étape est obligatoire"));
                }
            }
        }

        try {
            Recette updated = recetteService.updateFromDTO(id, dto);
            RecetteDTO responseDto = recetteMapper.toDTO(updated);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/persistance/recettes/{id}/valider - Valider une recette (actif=true, statut=VALIDEE)
     */
    @PutMapping("/{id}/valider")
    public ResponseEntity<RecetteDTO> validerRecette(@PathVariable Long id) {
        try {
            Recette recette = recetteService.validerRecette(id);
            return ResponseEntity.ok(recetteMapper.toDTO(recette));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/persistance/recettes/{id}/rejeter - Rejeter une recette (statut=REJETEE) avec motif
     */
    @PutMapping("/{id}/rejeter")
    public ResponseEntity<RecetteDTO> rejeterRecette(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String motif = body != null ? body.get("motif") : null;
        if (motif == null || motif.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            Recette recette = recetteService.rejeterRecette(id, motif.trim());
            return ResponseEntity.ok(recetteMapper.toDTO(recette));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

    /**
     * GET /api/persistance/recettes/validees - Récupérer recettes validées
     */
    @GetMapping("/validees")
    public ResponseEntity<List<RecetteDTO>> getRecettesValidees() {
        return ResponseEntity.ok(
            recetteService.findByStatut(StatutRecette.VALIDEE).stream()
                .map(recetteMapper::toDTO)
                .toList()
        );
    }

    /**
     * GET /api/persistance/recettes/rejetees - Récupérer recettes rejetées
     */
    @GetMapping("/rejetees")
    public ResponseEntity<List<RecetteDTO>> getRecettesRejetees() {
        return ResponseEntity.ok(
            recetteService.findByStatut(StatutRecette.REJETEE).stream()
                .map(recetteMapper::toDTO)
                .toList()
        );
    }


    // Méthode utilitaire
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
