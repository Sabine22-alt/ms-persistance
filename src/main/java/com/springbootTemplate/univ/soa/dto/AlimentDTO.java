package com.springbootTemplate.univ.soa.dto;

import com.springbootTemplate.univ.soa.model.Aliment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlimentDTO {
    private Long id;
    private String nom;
    private Float calories;
    private Float proteines;
    private Float glucides;
    private Float lipides;
    private Float fibres;
    private Aliment.CategorieAliment categorieAliment;
}