package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "aliments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aliment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column
    private Float calories;

    @Column
    private Float proteines;

    @Column
    private Float glucides;

    @Column
    private Float lipides;

    @Column
    private Float fibres;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private CategorieAliment categorieAliment;

    public enum CategorieAliment {
        FRUIT,
        LEGUME,
        VIANDE,
        POISSON,
        CEREALE,
        LAITIER,
        EPICE,
        GLUTEN,
        AUTRE
    }
}