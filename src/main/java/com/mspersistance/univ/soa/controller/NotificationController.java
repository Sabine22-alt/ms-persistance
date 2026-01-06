package com.mspersistance.univ.soa.controller;

import com.mspersistance.univ.soa.dto.NotificationDTO;
import com.mspersistance.univ.soa.mapper.NotificationMapper;
import com.mspersistance.univ.soa.model.Notification;
import com.mspersistance.univ.soa.service.RecetteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/persistance/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final RecetteService recetteService;
    private final NotificationMapper notificationMapper;

    @Autowired
    public NotificationController(RecetteService recetteService, NotificationMapper notificationMapper) {
        this.recetteService = recetteService;
        this.notificationMapper = notificationMapper;
    }

    /**
     * GET /api/persistance/notifications/utilisateur/{utilisateurId} - Récupérer toutes les notifications d'un utilisateur
     */
    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUtilisateur(@PathVariable Long utilisateurId) {
        List<NotificationDTO> notifications = recetteService.getNotificationsByUtilisateur(utilisateurId).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/persistance/notifications/utilisateur/{utilisateurId}/non-lues - Récupérer les notifications non lues
     */
    @GetMapping("/utilisateur/{utilisateurId}/non-lues")
    public ResponseEntity<List<NotificationDTO>> getNotificationsNonLues(@PathVariable Long utilisateurId) {
        List<NotificationDTO> notifications = recetteService.getNotificationsNonLues(utilisateurId).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/persistance/notifications/utilisateur/{utilisateurId}/count - Compter les notifications non lues
     */
    @GetMapping("/utilisateur/{utilisateurId}/count")
    public ResponseEntity<Map<String, Long>> countNotificationsNonLues(@PathVariable Long utilisateurId) {
        long count = recetteService.countNotificationsNonLues(utilisateurId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/persistance/notifications/{id}/lire - Marquer une notification comme lue
     */
    @PutMapping("/{id}/lire")
    public ResponseEntity<NotificationDTO> marquerCommeLue(@PathVariable Long id) {
        Notification notification = recetteService.marquerNotificationCommeLue(id);
        return ResponseEntity.ok(notificationMapper.toDTO(notification));
    }

    /**
     * PUT /api/persistance/notifications/utilisateur/{utilisateurId}/tout-lire - Marquer toutes les notifications comme lues
     */
    @PutMapping("/utilisateur/{utilisateurId}/tout-lire")
    public ResponseEntity<Map<String, String>> marquerToutesCommeLues(@PathVariable Long utilisateurId) {
        recetteService.marquerToutesNotificationsCommeLues(utilisateurId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Toutes les notifications ont été marquées comme lues");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/persistance/notifications/{id} - Supprimer une notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        recetteService.deleteNotification(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification supprimée avec succès");
        return ResponseEntity.ok(response);
    }
}

