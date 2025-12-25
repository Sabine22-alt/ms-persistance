package com.springbootTemplate.univ.soa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private Long utilisateurId;
    private Long recetteId;
    private String recetteTitre;
    private String type; // VALIDEE, REJETEE
    private String message;
    private Boolean lue;
    private LocalDateTime dateCreation;
}

