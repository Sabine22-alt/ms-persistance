package com.springbootTemplate.univ.soa.controller;

import com.springbootTemplate.univ.soa.dto.NotificationDTO;
import com.springbootTemplate.univ.soa.mapper.NotificationMapper;
import com.springbootTemplate.univ.soa.model.Notification;
import com.springbootTemplate.univ.soa.service.RecetteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("Tests unitaires pour NotificationController")
class NotificationControllerTest {

    @Mock
    private RecetteService recetteService;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationController notificationController;

    private Notification notification1;
    private Notification notification2;
    private NotificationDTO notificationDTO1;
    private NotificationDTO notificationDTO2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        notification1 = new Notification();
        notification1.setId(1L);
        notification1.setUtilisateurId(1L);
        notification1.setRecetteId(1L);
        notification1.setRecetteTitre("Recette validée");
        notification1.setType(Notification.TypeNotification.VALIDEE);
        notification1.setMessage("Votre recette a été validée");
        notification1.setLue(false);
        notification1.setDateCreation(LocalDateTime.now());

        notification2 = new Notification();
        notification2.setId(2L);
        notification2.setUtilisateurId(1L);
        notification2.setRecetteId(2L);
        notification2.setRecetteTitre("Recette rejetée");
        notification2.setType(Notification.TypeNotification.REJETEE);
        notification2.setMessage("Votre recette a été rejetée");
        notification2.setLue(true);
        notification2.setDateCreation(LocalDateTime.now());

        notificationDTO1 = new NotificationDTO();
        notificationDTO1.setId(1L);
        notificationDTO1.setUtilisateurId(1L);
        notificationDTO1.setRecetteId(1L);
        notificationDTO1.setRecetteTitre("Recette validée");
        notificationDTO1.setType("VALIDEE");
        notificationDTO1.setMessage("Votre recette a été validée");
        notificationDTO1.setLue(false);
        notificationDTO1.setDateCreation(LocalDateTime.now());

        notificationDTO2 = new NotificationDTO();
        notificationDTO2.setId(2L);
        notificationDTO2.setUtilisateurId(1L);
        notificationDTO2.setRecetteId(2L);
        notificationDTO2.setRecetteTitre("Recette rejetée");
        notificationDTO2.setType("REJETEE");
        notificationDTO2.setMessage("Votre recette a été rejetée");
        notificationDTO2.setLue(true);
        notificationDTO2.setDateCreation(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /notifications/utilisateur/{id} - Récupérer toutes les notifications")
    void testGetNotificationsByUtilisateur() {
        // Given
        when(recetteService.getNotificationsByUtilisateur(1L))
                .thenReturn(Arrays.asList(notification1, notification2));
        when(notificationMapper.toDTO(notification1)).thenReturn(notificationDTO1);
        when(notificationMapper.toDTO(notification2)).thenReturn(notificationDTO2);

        // When
        ResponseEntity<List<NotificationDTO>> response = notificationController.getNotificationsByUtilisateur(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(recetteService).getNotificationsByUtilisateur(1L);
    }

    @Test
    @DisplayName("GET /notifications/utilisateur/{id}/non-lues - Récupérer notifications non lues")
    void testGetNotificationsNonLues() {
        // Given
        when(recetteService.getNotificationsNonLues(1L))
                .thenReturn(Arrays.asList(notification1));
        when(notificationMapper.toDTO(notification1)).thenReturn(notificationDTO1);

        // When
        ResponseEntity<List<NotificationDTO>> response = notificationController.getNotificationsNonLues(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertFalse(response.getBody().get(0).getLue());
        verify(recetteService).getNotificationsNonLues(1L);
    }

    @Test
    @DisplayName("GET /notifications/utilisateur/{id}/count - Compter notifications non lues")
    void testCountNotificationsNonLues() {
        // Given
        when(recetteService.countNotificationsNonLues(1L)).thenReturn(3L);

        // When
        ResponseEntity<Map<String, Long>> response = notificationController.countNotificationsNonLues(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3L, response.getBody().get("count"));
        verify(recetteService).countNotificationsNonLues(1L);
    }

    @Test
    @DisplayName("PUT /notifications/{id}/lire - Marquer notification comme lue")
    void testMarquerCommeLue() {
        // Given
        notification1.setLue(true);
        notificationDTO1.setLue(true);
        when(recetteService.marquerNotificationCommeLue(1L)).thenReturn(notification1);
        when(notificationMapper.toDTO(notification1)).thenReturn(notificationDTO1);

        // When
        ResponseEntity<NotificationDTO> response = notificationController.marquerCommeLue(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getLue());
        verify(recetteService).marquerNotificationCommeLue(1L);
    }

    @Test
    @DisplayName("PUT /notifications/utilisateur/{id}/tout-lire - Marquer toutes comme lues")
    void testMarquerToutesCommeLues() {
        // Given
        doNothing().when(recetteService).marquerToutesNotificationsCommeLues(1L);

        // When
        ResponseEntity<Map<String, String>> response = notificationController.marquerToutesCommeLues(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Toutes les notifications ont été marquées comme lues", response.getBody().get("message"));
        verify(recetteService).marquerToutesNotificationsCommeLues(1L);
    }
}

