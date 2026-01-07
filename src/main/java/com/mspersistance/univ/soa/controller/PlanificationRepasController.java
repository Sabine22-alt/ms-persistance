package com.mspersistance.univ.soa.controller;

import com.mspersistance.univ.soa.dto.PlanificationRepasDTO;
import com.mspersistance.univ.soa.mapper.PlanificationRepasMapper;
import com.mspersistance.univ.soa.model.PlanificationRepas;
import com.mspersistance.univ.soa.service.PlanificationRepasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/persistance/utilisateurs")
@CrossOrigin(origins = "*")
public class PlanificationRepasController {

    private final PlanificationRepasService planificationRepasService;
    private final PlanificationRepasMapper planificationRepasMapper;

    @Autowired
    PlanificationRepasController(PlanificationRepasService planificationRepasService,
                              PlanificationRepasMapper planificationRepasMapper) {
        this.planificationRepasService = planificationRepasService;
        this.planificationRepasMapper = planificationRepasMapper;
    }

    /**
     * GET /api/persistance/utilisateurs/{utilisateurId}/planification/{semaine}/{annee}
     * RÃ©cupÃ¨re la planification pour une semaine spÃ©cifique
     */
    @GetMapping("/{utilisateurId}/planification/{semaine}/{annee}")
    public ResponseEntity<?> getPlanification(@PathVariable Long utilisateurId,
                                              @PathVariable Integer semaine,
                                              @PathVariable Integer annee) {
        var planif = planificationRepasService.getPlanification(utilisateurId, semaine, annee);

        if (planif.isEmpty()) {
            // CrÃ©er une planification vide si elle n'existe pas
            PlanificationRepas newPlanif = planificationRepasService
                .getOrCreatePlanification(utilisateurId, semaine, annee);
            return ResponseEntity.ok(planificationRepasMapper.toDTO(newPlanif));
        }

        return ResponseEntity.ok(planificationRepasMapper.toDTO(planif.get()));
    }

    /**
     * POST /api/persistance/utilisateurs/{utilisateurId}/planification/{semaine}/{annee}/jour/{jour}/repas
     * Ajouter/modifier un repas pour un jour
     */
    @PostMapping("/{utilisateurId}/planification/{semaine}/{annee}/jour/{jour}/repas")
    @Transactional
    public ResponseEntity<?> addRepas(
        @PathVariable Long utilisateurId,
        @PathVariable Integer semaine,
        @PathVariable Integer annee,
        @PathVariable Integer jour,
        @RequestBody Map<String, Object> request) {

        try {
            Integer typeRepas = ((Number) request.get("typeRepas")).intValue();
            Long recetteId = request.get("recetteId") != null ?
                ((Number) request.get("recetteId")).longValue() : null;
            String noteLibre = (String) request.get("noteLibre");

            if (typeRepas == null || (typeRepas < 0 || typeRepas > 2)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Type de repas invalide (0=petit-dÃ©j, 1=dÃ©jeuner, 2=dÃ®ner)"));
            }

            if (jour < 0 || jour > 6) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Jour invalide (0=lundi, 6=dimanche)"));
            }

            PlanificationRepas planif = planificationRepasService.addOrUpdateRepas(
                utilisateurId, semaine, annee, jour, typeRepas, recetteId, noteLibre);

            return ResponseEntity.ok(planificationRepasMapper.toDTO(planif));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur : " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/persistance/utilisateurs/{utilisateurId}/planification/{semaine}/{annee}/jour/{jour}/repas/{typeRepas}
     * Supprimer un repas planifiÃ©
     */
    @DeleteMapping("/{utilisateurId}/planification/{semaine}/{annee}/jour/{jour}/repas/{typeRepas}")
    @Transactional
    public ResponseEntity<?> deleteRepas(
        @PathVariable Long utilisateurId,
        @PathVariable Integer semaine,
        @PathVariable Integer annee,
        @PathVariable Integer jour,
        @PathVariable Integer typeRepas) {

        try {
            if (typeRepas < 0 || typeRepas > 2) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Type de repas invalide"));
            }

            PlanificationRepas planif = planificationRepasService.deleteRepas(
                utilisateurId, semaine, annee, jour, typeRepas);

            return ResponseEntity.ok(planificationRepasMapper.toDTO(planif));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Planification non trouvÃ©e"));
        }
    }

    /**
     * GET /api/persistance/utilisateurs/{utilisateurId}/planifications/historique
     * RÃ©cupÃ¨re l'historique des planifications
     */
    @GetMapping("/{utilisateurId}/planifications/historique")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PlanificationRepasDTO>> getPlanificationsHistory(@PathVariable Long utilisateurId) {
        List<PlanificationRepasDTO> dtos = planificationRepasService.getPlanificationsHistory(utilisateurId)
            .stream()
            .map(planificationRepasMapper::toDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}

