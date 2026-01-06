package com.mspersistance.univ.soa.dto;

import java.time.LocalDateTime;

/**
 * DTO Notification en Record - Immutable et performant
 */
public record NotificationDTO(
    Long id,
    Long utilisateurId,
    Long recetteId,
    String recetteTitre,
    String type, // VALIDEE, REJETEE, EN_ATTENTE
    String message,
    Boolean lue,
    LocalDateTime dateCreation
) {}

