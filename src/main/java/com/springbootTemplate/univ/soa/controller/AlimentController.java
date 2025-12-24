package com.springbootTemplate.univ.soa.controller;

import com.springbootTemplate.univ.soa.dto.AlimentDTO;
import com.springbootTemplate.univ.soa.mapper.AlimentMapper;
import com.springbootTemplate.univ.soa.model.Aliment;
import com.springbootTemplate.univ.soa.service.AlimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/persistance/aliments")
@CrossOrigin(origins = "*")
public class AlimentController {

    @Autowired
    private AlimentService alimentService;

    @Autowired
    private AlimentMapper alimentMapper;

    /**
     * GET /api/persistance/aliments - Récupérer tous les aliments
     */
    @GetMapping
    public ResponseEntity<List<AlimentDTO>> getAllAliments() {
        List<AlimentDTO> dtos = alimentService.findAll().stream()
                .map(alimentMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/aliments/{id} - Récupérer un aliment par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlimentDTO> getAlimentById(@PathVariable Long id) {
        return alimentService.findById(id)
                .map(alimentMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/persistance/aliments - Créer un nouvel aliment
     */
    @PostMapping
    public ResponseEntity<?> createAliment(@RequestBody AlimentDTO dto) {
        // Validation : nom requis
        if (dto.getNom() == null || dto.getNom().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le nom de l'aliment est obligatoire"));
        }

        // Validation : longueur du nom
        if (dto.getNom().trim().length() < 2) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le nom doit contenir au moins 2 caractères"));
        }

        if (dto.getNom().length() > 100) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le nom ne peut pas dépasser 100 caractères"));
        }

        // Validation : nom unique
        if (alimentService.findByNom(dto.getNom().trim()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Un aliment avec ce nom existe déjà"));
        }

        // Validation : catégorie requise
        if (dto.getCategorieAliment() == null) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("La catégorie est obligatoire"));
        }

        // Validation : catégorie valide
        try {
            Aliment.CategorieAliment.valueOf(dto.getCategorieAliment().name());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Catégorie invalide. Valeurs acceptées: " +
                            "FRUIT, LEGUME, VIANDE, POISSON, CEREALE, LAITIER, EPICE, GLUTEN, AUTRE"));
        }

        try {
            Aliment aliment = alimentMapper.toEntity(dto);
            Aliment saved = alimentService.save(aliment);
            AlimentDTO responseDto = alimentMapper.toDTO(saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la création: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/persistance/aliments/{id} - Mettre à jour un aliment
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAliment(
            @PathVariable Long id,
            @RequestBody AlimentDTO dto) {

        // Vérifier que l'aliment existe
        if (alimentService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Aliment non trouvé avec l'ID: " + id));
        }

        // Validation : nom requis
        if (dto.getNom() == null || dto.getNom().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le nom de l'aliment est obligatoire"));
        }

        // Validation : longueur du nom
        if (dto.getNom().trim().length() < 2) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le nom doit contenir au moins 2 caractères"));
        }

        if (dto.getNom().length() > 100) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le nom ne peut pas dépasser 100 caractères"));
        }

        // Validation : nom unique (sauf pour l'aliment actuel)
        alimentService.findByNom(dto.getNom().trim()).ifPresent(existingAliment -> {
            if (!existingAliment.getId().equals(id)) {
                throw new IllegalStateException("Un autre aliment utilise déjà ce nom");
            }
        });

        // Validation : catégorie requise
        if (dto.getCategorieAliment() == null) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("La catégorie est obligatoire"));
        }

        try {
            Aliment aliment = alimentMapper.toEntity(dto);
            Aliment updated = alimentService.update(id, aliment);
            AlimentDTO responseDto = alimentMapper.toDTO(updated);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Gérer à la fois IllegalArgumentException et IllegalStateException comme CONFLICT (409)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }


    /**
     * DELETE /api/persistance/aliments/{id} - Supprimer un aliment
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAliment(@PathVariable Long id) {
        // Vérifier que l'aliment existe
        if (alimentService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Aliment non trouvé avec l'ID: " + id));
        }

        try {
            alimentService.deleteById(id);
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