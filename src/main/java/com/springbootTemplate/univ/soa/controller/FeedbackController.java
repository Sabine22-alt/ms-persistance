package com.springbootTemplate.univ.soa.controller;

import com.springbootTemplate.univ.soa.dto.FeedbackDTO;
import com.springbootTemplate.univ.soa.mapper.FeedbackMapper;
import com.springbootTemplate.univ.soa.model.Feedback;
import com.springbootTemplate.univ.soa.service.FeedbackService;
import com.springbootTemplate.univ.soa.service.RecetteService;
import com.springbootTemplate.univ.soa.service.UtilisateurService;
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

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private RecetteService recetteService;

    @Autowired
    private FeedbackMapper feedbackMapper;

    /**
     * GET /api/persistance/feedbacks - Récupérer tous les feedbacks
     */
    @GetMapping
    public ResponseEntity<List<FeedbackDTO>> getAllFeedbacks() {
        List<FeedbackDTO> dtos = feedbackService.findAll().stream()
                .map(feedbackMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/feedbacks/{id} - Récupérer un feedback par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDTO> getFeedbackById(@PathVariable Long id) {
        return feedbackService.findById(id)
                .map(feedbackMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/persistance/feedbacks/utilisateur/{utilisateurId} - Récupérer les feedbacks d'un utilisateur
     */
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByUtilisateur(@PathVariable Long utilisateurId) {
        List<FeedbackDTO> dtos = feedbackService.findByUtilisateurId(utilisateurId).stream()
                .map(feedbackMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/feedbacks/recette/{recetteId} - Récupérer les feedbacks d'une recette
     */
    @GetMapping("/recette/{recetteId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByRecette(@PathVariable Long recetteId) {
        List<FeedbackDTO> dtos = feedbackService.findByRecetteId(recetteId).stream()
                .map(feedbackMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * POST /api/persistance/feedbacks - Créer un nouveau feedback
     */
    @PostMapping
    public ResponseEntity<?> createFeedback(@RequestBody FeedbackDTO dto) {
        logger.info("DEBUG createFeedback - Requête reçue: utilisateurId={}, recetteId={}, evaluation={}, commentaire={}",
            dto.getUtilisateurId(), dto.getRecetteId(), dto.getEvaluation(), dto.getCommentaire());

        // Validation : utilisateurId requis
        if (dto.getUtilisateurId() == null) {
            logger.warn("Validation échouée: utilisateurId est null");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'ID de l'utilisateur est obligatoire"));
        }

        // Validation : recetteId requis
        if (dto.getRecetteId() == null) {
            logger.warn("Validation échouée: recetteId est null");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'ID de la recette est obligatoire"));
        }

        // Validation : l'utilisateur existe
        if (utilisateurService.findById(dto.getUtilisateurId()).isEmpty()) {
            logger.warn("Utilisateur non trouvé avec l'ID: {}", dto.getUtilisateurId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Utilisateur non trouvé avec l'ID: " + dto.getUtilisateurId()));
        }

        // Validation : la recette existe
        if (recetteService.findById(dto.getRecetteId()).isEmpty()) {
            logger.warn("Recette non trouvée avec l'ID: {}", dto.getRecetteId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Recette non trouvée avec l'ID: " + dto.getRecetteId()));
        }

        // Validation : évaluation requise
        if (dto.getEvaluation() == null) {
            logger.warn("Validation échouée: evaluation est null");
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'évaluation est obligatoire"));
        }

        // Validation : évaluation entre 1 et 5
        if (dto.getEvaluation() < 1 || dto.getEvaluation() > 5) {
            logger.warn("Évaluation invalide: {}. Doit être entre 1 et 5", dto.getEvaluation());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'évaluation doit être comprise entre 1 et 5 étoiles"));
        }

        // Validation : commentaire (optionnel mais avec longueur max)
        if (dto.getCommentaire() != null && dto.getCommentaire().length() > 1000) {
            logger.warn("Commentaire trop long: {} caractères (max: 1000)", dto.getCommentaire().length());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le commentaire ne peut pas dépasser 1000 caractères"));
        }

        // VALIDATION CRUCIALE : Vérifier qu'un feedback n'existe pas déjà pour cet utilisateur et cette recette
        if (feedbackService.existsByUtilisateurIdAndRecetteId(dto.getUtilisateurId(), dto.getRecetteId())) {
            logger.warn("Un feedback existe déjà pour utilisateurId={} et recetteId={}", dto.getUtilisateurId(), dto.getRecetteId());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Vous avez déjà noté cette recette."));
        }

        try {
            logger.info("Toutes les validations sont passées. Création du feedback...");
            Feedback feedback = feedbackMapper.toEntity(dto);
            logger.debug("Feedback mapper toEntity réussi");

            Feedback saved = feedbackService.save(feedback, dto.getUtilisateurId(), dto.getRecetteId());
            logger.info("Feedback créé avec succès. ID: {}", saved.getId());

            FeedbackDTO responseDto = feedbackMapper.toDTO(saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du feedback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la création: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/persistance/feedbacks/{id} - Mettre à jour un feedback
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFeedback(
            @PathVariable Long id,
            @RequestBody FeedbackDTO dto) {

        // Vérifier que le feedback existe
        if (feedbackService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Feedback non trouvé avec l'ID: " + id));
        }

        // Validation : évaluation requise
        if (dto.getEvaluation() == null) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'évaluation est obligatoire"));
        }

        // Validation : évaluation entre 1 et 5
        if (dto.getEvaluation() < 1 || dto.getEvaluation() > 5) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'évaluation doit être comprise entre 1 et 5 étoiles"));
        }

        // Validation : commentaire longueur
        if (dto.getCommentaire() != null && dto.getCommentaire().length() > 1000) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le commentaire ne peut pas dépasser 1000 caractères"));
        }

        try {
            Feedback feedback = feedbackMapper.toEntity(dto);
            Feedback updated = feedbackService.update(id, feedback);
            FeedbackDTO responseDto = feedbackMapper.toDTO(updated);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/persistance/feedbacks/{id} - Supprimer un feedback
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        // Vérifier que le feedback existe
        if (feedbackService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Feedback non trouvé avec l'ID: " + id));
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

            return ResponseEntity.ok(createSuccessResponse("Feedbacks supprimés: " + feedbacks.size()));
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

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> success = new HashMap<>();
        success.put("message", message);
        return success;
    }
}