package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recette_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ingredient_recette"))
    @JsonBackReference
    private Recette recette;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aliment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ingredient_aliment"))
    private Aliment aliment;

    @Column
    private Float quantite;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Unite unite;

    @Column
    private Boolean principal = false;

    public enum Unite {
        GRAMME,
        KILOGRAMME,
        LITRE,
        MILLILITRE,
        CUILLERE_A_SOUPE,
        CUILLERE_A_CAFE,
        SACHET,
        UNITE
    }
}