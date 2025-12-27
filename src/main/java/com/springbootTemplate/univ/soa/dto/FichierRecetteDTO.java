package com.springbootTemplate.univ.soa.dto;

import com.springbootTemplate.univ.soa.model.FichierRecette;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FichierRecetteDTO {
    private Long id;
    private String nomOriginal;
    private String nomStocke;
    private String contentType;
    private Long taille;
    private FichierRecette.TypeFichier type;
    private String urlTelechargement;
    private String urlStream;
    private Long recetteId;
    private LocalDateTime dateUpload;
}
