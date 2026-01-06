package com.mspersistance.univ.soa.dto;

import com.mspersistance.univ.soa.model.Aliment;

/**
 * DTO Aliment en Record - Immutable et performant
 */
public record AlimentDTO(
    Long id,
    String nom,
    Float calories,
    Float proteines,
    Float glucides,
    Float lipides,
    Float fibres,
    Aliment.CategorieAliment categorieAliment
) {}
