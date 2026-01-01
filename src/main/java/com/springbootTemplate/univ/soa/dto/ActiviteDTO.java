package com.springbootTemplate.univ.soa.dto;

import com.springbootTemplate.univ.soa.model.Activite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiviteDTO {
    private Long id;
    private Long utilisateurId;
    private Activite.TypeActivite type;
    private String description;
    private LocalDateTime dateActivite;
    private String details;
}

