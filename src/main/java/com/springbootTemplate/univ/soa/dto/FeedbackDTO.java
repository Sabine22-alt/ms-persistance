package com.springbootTemplate.univ.soa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {

    private Long id;
    private Long utilisateurId;
    private Long recetteId;
    private Integer evaluation;
    private String commentaire;
    private LocalDateTime dateFeedback;
    private LocalDateTime dateModification;
}