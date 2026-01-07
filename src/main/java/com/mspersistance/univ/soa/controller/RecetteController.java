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
     * GET /api/persistance/recettes - RÃ©cupÃ©rer toutes les recettes (VERSION OPTIMISÃ‰E)
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
                .map(recetteMapper::toDTOLight) // VERSION LÃ‰GÃˆRE sans collections
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/recettes/en-attente - RÃ©cupÃ©rer recettes en attente de validation
     */
    @GetMapping("/en-attente")
    @Transactional(readOnly = true)
    public ResponseEntity<List<RecetteDTO>> getRecettesEnAttente() {
        List<RecetteDTO> dtos = recetteService.findByStatut(StatutRecette.EN_ATTENTE).stream()
                .map(recetteMapper::toDTOLight) // VERSION LÃ‰GÃˆRE
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/recettes/utilisateur/{utilisateurId} - RÃ©cupÃ©rer les recettes d'un utilisateur
     */
    @GetMapping("/utilisateur/{utilisateurId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<RecetteDTO>> getRecettesByUtilisateur(@PathVariable Long utilisateurId) {
        List<RecetteDTO> dtos = recetteService.findByUtilisateurId(utilisateurId).stream()
                .map(recetteMapper::toDTOLight) // VERSION LÃ‰GÃˆRE
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/recettes/{id} - RÃ©cupÃ©rer une recette par ID (VERSION COMPLÃˆTE)
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<RecetteDTO> getRecetteById(@PathVariable Long id) {
        return recetteService.findById(id)
                .map(recetteMapper::toDTO) // VERSION COMPLÃˆTE avec collections
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/persistance/recettes - CrÃ©er une nouvelle recette
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
                    .body(createErrorResponse("Le titre doit contenir au moins 3 caractÃ¨res"));
        }

        if (dto.titre().length() > 200) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre ne peut pas dÃ©passer 200 caractÃ¨res"));
        }

        // Validation : temps total
        if (dto.tempsTotal() != null && dto.tempsTotal() <= 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le temps total doit Ãªtre supÃ©rieur Ã  0"));
        }

        if (dto.tempsTotal() != null && dto.tempsTotal() > 1440) { // 24h max
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le temps total ne peut pas dÃ©passer 1440 minutes (24h)"));
        }

        // Validation : kcal
        if (dto.kcal() != null && dto.kcal() < 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Les calories ne peuvent pas Ãªtre nÃ©gatives"));
        }

        if (dto.kcal() != null && dto.kcal() > 10000) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Les calories semblent excessives (max 10000)"));
        }

        // Validation : difficultÃ©
        if (dto.difficulte() != null) {
            try {
                Recette.Difficulte.valueOf(dto.difficulte().name());
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("DifficultÃ© invalide. Valeurs acceptÃ©es: FACILE, MOYEN, DIFFICILE"));
            }
        }

        // Validation : ingrÃ©dients
        if (dto.ingredients() != null && !dto.ingredients().isEmpty()) {
            for (RecetteDTO.IngredientDTO ingredient : dto.ingredients()) {
                // Validation : alimentId OU alimentNom requis
                if (ingredient.alimentId() == null &&
                    (ingredient.alimentNom() == null || ingredient.alimentNom().trim().isEmpty())) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ID ou le nom de l'aliment est requis pour chaque ingrÃ©dient"));
                }

                // Validation : quantitÃ©
                if (ingredient.quantite() != null && ingredient.quantite() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("La quantitÃ© doit Ãªtre supÃ©rieure Ã  0"));
                }

                // Validation : unitÃ©
                if (ingredient.unite() != null && !ingredient.unite().isEmpty()) {
                    try {
                        Ingredient.Unite.valueOf(ingredient.unite());
                    } catch (Exception e) {
                        return ResponseEntity.badRequest()
                                .body(createErrorResponse("UnitÃ© invalide pour un ingrÃ©dient. Valeurs acceptÃ©es: " +
                                        "GRAMME, KILOGRAMME, LITRE, MILLILITRE, CUILLERE_A_SOUPE, CUILLERE_A_CAFE, SACHET, UNITE"));
                    }
                }
            }
        }

        // Validation : Ã©tapes
        if (dto.etapes() != null && !dto.etapes().isEmpty()) {
            for (RecetteDTO.EtapeDTO etape : dto.etapes()) {
                // Validation : ordre requis
                if (etape.ordre() == null || etape.ordre() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ordre de chaque Ã©tape doit Ãªtre supÃ©rieur Ã  0"));
                }

                // Validation : texte requis
                if (etape.texte() == null || etape.texte().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le texte de chaque Ã©tape est obligatoire"));
                }

                // Validation : longueur texte
                if (etape.texte().trim().length() < 5) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le texte de chaque Ã©tape doit contenir au moins 5 caractÃ¨res"));
                }

                // Validation : temps Ã©tape
                if (etape.temps() != null && etape.temps() < 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le temps d'une Ã©tape ne peut pas Ãªtre nÃ©gatif"));
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
                    .body(createErrorResponse("Erreur lors de la crÃ©ation: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/persistance/recettes/{id} - Mettre Ã  jour une recette
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecette(
            @PathVariable Long id,
            @RequestBody RecetteDTO dto) {

        // VÃ©rifier que la recette existe
        if (recetteService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Recette non trouvÃ©e avec l'ID: " + id));
        }

        // Appliquer les mÃªmes validations que pour la crÃ©ation
        if (dto.titre() == null || dto.titre().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre de la recette est obligatoire"));
        }

        if (dto.titre().trim().length() < 3) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre doit contenir au moins 3 caractÃ¨res"));
        }

        if (dto.titre().length() > 200) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le titre ne peut pas dÃ©passer 200 caractÃ¨res"));
        }

        if (dto.tempsTotal() != null && dto.tempsTotal() <= 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le temps total doit Ãªtre supÃ©rieur Ã  0"));
        }

        if (dto.kcal() != null && dto.kcal() < 0) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Les calories ne peuvent pas Ãªtre nÃ©gatives"));
        }

        // Validation des ingrÃ©dients
        if (dto.ingredients() != null) {
            for (RecetteDTO.IngredientDTO ingredient : dto.ingredients()) {
                // Validation : alimentId OU alimentNom requis
                if (ingredient.alimentId() == null &&
                    (ingredient.alimentNom() == null || ingredient.alimentNom().trim().isEmpty())) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ID ou le nom de l'aliment est requis pour chaque ingrÃ©dient"));
                }
                if (ingredient.quantite() != null && ingredient.quantite() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("La quantitÃ© doit Ãªtre supÃ©rieure Ã  0"));
                }
            }
        }

        // Validation des Ã©tapes
        if (dto.etapes() != null) {
            for (RecetteDTO.EtapeDTO etape : dto.etapes()) {
                if (etape.ordre() == null || etape.ordre() <= 0) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("L'ordre de chaque Ã©tape doit Ãªtre supÃ©rieur Ã  0"));
                }
                if (etape.texte() == null || etape.texte().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Le texte de chaque Ã©tape est obligatoire"));
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
                    .body(createErrorResponse("Erreur lors de la mise Ã  jour: " + e.getMessage()));
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
        // VÃ©rifier que la recette existe
        if (recetteService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Recette non trouvÃ©e avec l'ID: " + id));
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
     * GET /api/persistance/recettes/validees - RÃ©cupÃ©rer recettes validÃ©es
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
     * GET /api/persistance/recettes/rejetees - RÃ©cupÃ©rer recettes rejetÃ©es
     */
    @GetMapping("/rejetees")
    public ResponseEntity<List<RecetteDTO>> getRecettesRejetees() {
        return ResponseEntity.ok(
            recetteService.findByStatut(StatutRecette.REJETEE).stream()
                .map(recetteMapper::toDTO)
                .toList()
        );
    }


    // MÃ©thode utilitaire
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
