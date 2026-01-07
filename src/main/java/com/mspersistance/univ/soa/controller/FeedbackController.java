package com.mspersistance.univ.soa.controller;

import com.mspersistance.univ.soa.dto.FeedbackDTO;
import com.mspersistance.univ.soa.mapper.FeedbackMapper;
import com.mspersistance.univ.soa.model.Feedback;
import com.mspersistance.univ.soa.service.FeedbackService;
import com.mspersistance.univ.soa.service.RecetteService;
import com.mspersistance.univ.soa.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/persistance/feedbacks")
@CrossOrigin(origins = "*")
public class FeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);
    private final FeedbackService feedbackService;
    private final UtilisateurService utilisateurService;
    private final RecetteService recetteService;
    private final FeedbackMapper feedbackMapper;

    @Autowired
    FeedbackController(FeedbackService feedbackService,
                       UtilisateurService utilisateurService,
                       RecetteService recetteService,
                       FeedbackMapper feedbackMapper) {
        this.feedbackService = feedbackService;
        this.utilisateurService = utilisateurService;
        this.recetteService = recetteService;
        this.feedbackMapper = feedbackMapper;
    }

    /**
     * GET /api/persistance/feedbacks - RÃ©cupÃ©rer tous les feedbacks
     */
    @GetMapping
    public ResponseEntity<List<FeedbackDTO>> getAllFeedbacks() {
        List<FeedbackDTO> dtos = feedbackService.findAll().stream()
                .map(feedbackMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/feedbacks/{id} - RÃ©cupÃ©rer un feedback par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDTO> getFeedbackById(@PathVariable Long id) {
        return feedbackService.findById(id)
                .map(feedbackMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/persistance/feedbacks/utilisateur/{utilisateurId} - RÃ©cupÃ©rer les feedbacks d'un utilisateur
     */
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByUtilisateur(@PathVariable Long utilisateurId) {
        List<FeedbackDTO> dtos = feedbackService.findByUtilisateurId(utilisateurId).stream()
                .map(feedbackMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/feedbacks/recette/{recetteId} - RÃ©cupÃ©rer les feedbacks d'une recette
     */
    @GetMapping("/recette/{recetteId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByRecette(@PathVariable Long recetteId) {
        List<FeedbackDTO> dtos = feedbackService.findByRecetteId(recetteId).stream()
                .map(feedbackMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * POST /api/persistance/feedbacks - CrÃ©er un nouveau feedback
     */
    @PostMapping
    public ResponseEntity<?> createFeedback(@RequestBody FeedbackDTO dto) {
        try {
            if (dto.utilisateurId() == null) {
                logger.warn("Validation Ã©chouÃ©e: utilisateurId est null");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("L'ID de l'utilisateur est obligatoire"));
            }

            if (dto.recetteId() == null) {
                logger.warn("Validation Ã©chouÃ©e: recetteId est null");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("L'ID de la recette est obligatoire"));
            }

            if (dto.evaluation() == null) {
                logger.warn("Validation Ã©chouÃ©e: evaluation est null");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("L'Ã©valuation est obligatoire"));
            }

            if (dto.evaluation() < 1 || dto.evaluation() > 5) {
                logger.warn("Ã‰valuation invalide: {}. Doit Ãªtre entre 1 et 5", dto.evaluation());
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("L'Ã©valuation doit Ãªtre comprise entre 1 et 5 Ã©toiles"));
            }

            // Validation : commentaire (optionnel mais avec longueur max)
            if (dto.commentaire() != null && dto.commentaire().length() > 1000) {
                logger.warn("Commentaire trop long: {} caractÃ¨res (max: 1000)", dto.commentaire().length());
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Le commentaire ne peut pas dÃ©passer 1000 caractÃ¨res"));
            }

            // VALIDATION CRUCIALE : VÃ©rifier qu'un feedback n'existe pas dÃ©jÃ  pour cet utilisateur et cette recette
            if (feedbackService.existsByUtilisateurIdAndRecetteId(dto.utilisateurId(), dto.recetteId())) {
                logger.warn("Un feedback existe dÃ©jÃ  pour utilisateurId={} et recetteId={}", dto.utilisateurId(), dto.recetteId());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse("Vous avez dÃ©jÃ  notÃ© cette recette."));
            }

            logger.info("Toutes les validations sont passÃ©es. CrÃ©ation du feedback...");
            Feedback feedback = feedbackMapper.toEntity(dto);
            logger.debug("Feedback mapper toEntity rÃ©ussi");

            Feedback saved = feedbackService.save(feedback, dto.utilisateurId(), dto.recetteId());
            logger.info("Feedback crÃ©Ã© avec succÃ¨s. ID: {}", saved.getId());

            FeedbackDTO responseDto = feedbackMapper.toDTO(saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException ex) {
            logger.warn("Validation failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(ex.getMessage()));
        } catch (Exception e) {
            logger.error("Erreur lors de la crÃ©ation du feedback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * PUT /api/persistance/feedbacks/{id} - Mettre Ã  jour un feedback
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFeedback(
            @PathVariable Long id,
            @RequestBody FeedbackDTO dto) {

        // VÃ©rifier que le feedback existe
        if (feedbackService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Feedback non trouvÃ© avec l'ID: " + id));
        }

        // Validation : Ã©valuation requise
        if (dto.evaluation() == null) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'Ã©valuation est obligatoire"));
        }

        // Validation : Ã©valuation entre 1 et 5
        if (dto.evaluation() < 1 || dto.evaluation() > 5) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'Ã©valuation doit Ãªtre comprise entre 1 et 5 Ã©toiles"));
        }

        // Validation : commentaire longueur
        if (dto.commentaire() != null && dto.commentaire().length() > 1000) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le commentaire ne peut pas dÃ©passer 1000 caractÃ¨res"));
        }

        try {
            Feedback feedback = feedbackMapper.toEntity(dto);
            Feedback updated = feedbackService.update(id, feedback);
            FeedbackDTO responseDto = feedbackMapper.toDTO(updated);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la mise Ã  jour: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/persistance/feedbacks/{id} - Supprimer un feedback
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        // VÃ©rifier que le feedback existe
        if (feedbackService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Feedback non trouvÃ© avec l'ID: " + id));
        }

        try {
            feedbackService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/persistance/feedbacks/utilisateur/{utilisateurId}/recette/{recetteId} - Supprimer les feedbacks d'un utilisateur pour une recette
     */
    @DeleteMapping("/utilisateur/{utilisateurId}/recette/{recetteId}")
    public ResponseEntity<?> deleteFeedbackByUtilisateurAndRecette(
            @PathVariable Long utilisateurId,
            @PathVariable Long recetteId) {
        try {
            List<Feedback> feedbacks = feedbackService.findByUtilisateurId(utilisateurId).stream()
                    .filter(f -> f.getRecette().getId().equals(recetteId))
                    .toList();

            feedbacks.forEach(f -> feedbackService.deleteById(f.getId()));

            return ResponseEntity.ok(createSuccessResponse("Feedbacks supprimÃ©s: " + feedbacks.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    // MÃ©thode utilitaire
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> success = new HashMap<>();
        success.put("message", message);
        return success;
    }
}
