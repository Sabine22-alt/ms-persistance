package com.mspersistance.univ.soa.mapper;

import com.mspersistance.univ.soa.dto.NotificationDTO;
import com.mspersistance.univ.soa.model.Notification;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Notification et NotificationDTO (Record).
 */
@Component
public class NotificationMapper {

    /**
     * Convertit une entité Notification en DTO Record
     */
    public NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        return new NotificationDTO(
                notification.getId(),
                notification.getUtilisateurId(),
                notification.getRecetteId(),
                notification.getRecetteTitre(),
                notification.getType() != null ? notification.getType().name() : null,
                notification.getMessage(),
                notification.getLue(),
                notification.getDateCreation()
        );
    }

    /**
     * Convertit un DTO Record en entité Notification
     */
    public Notification toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setId(dto.id());
        notification.setUtilisateurId(dto.utilisateurId());
        notification.setRecetteId(dto.recetteId());
        notification.setRecetteTitre(dto.recetteTitre());
        notification.setType(dto.type() != null ? Notification.TypeNotification.valueOf(dto.type()) : null);
        notification.setMessage(dto.message());
        notification.setLue(dto.lue());
        return notification;
    }
}

