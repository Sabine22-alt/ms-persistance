package com.mspersistance.univ.soa.dto;

import com.mspersistance.univ.soa.model.Activite;

import java.time.LocalDateTime;

/**
 * DTO Activite en Record - Immutable et performant
 */
public record ActiviteDTO(
    Long id,
    Long utilisateurId,
    Activite.TypeActivite type,
    String description,
    LocalDateTime dateActivite,
    String details
) {}

