package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "etapes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Etape {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recette_id", nullable = false, foreignKey = @ForeignKey(name = "fk_etape_recette"))
    @JsonBackReference
    private Recette recette;

    @Column(nullable = false)
    private Integer ordre;

    @Column
    private Integer temps; // en minutes

    @Column(columnDefinition = "TEXT", nullable = false)
    private String texte;
}