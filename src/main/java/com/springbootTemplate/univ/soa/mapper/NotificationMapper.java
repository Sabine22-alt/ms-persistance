package com.springbootTemplate.univ.soa.mapper;

import com.springbootTemplate.univ.soa.dto.NotificationDTO;
import com.springbootTemplate.univ.soa.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUtilisateurId(notification.getUtilisateurId());
        dto.setRecetteId(notification.getRecetteId());
        dto.setRecetteTitre(notification.getRecetteTitre());
        dto.setType(notification.getType() != null ? notification.getType().name() : null);
        dto.setMessage(notification.getMessage());
        dto.setLue(notification.getLue());
        dto.setDateCreation(notification.getDateCreation());
        return dto;
    }

    public Notification toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setUtilisateurId(dto.getUtilisateurId());
        notification.setRecetteId(dto.getRecetteId());
        notification.setRecetteTitre(dto.getRecetteTitre());
        notification.setType(dto.getType() != null ? Notification.TypeNotification.valueOf(dto.getType()) : null);
        notification.setMessage(dto.getMessage());
        notification.setLue(dto.getLue());
        return notification;
    }
}

