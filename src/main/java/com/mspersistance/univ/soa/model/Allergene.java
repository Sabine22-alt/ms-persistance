package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "allergenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Allergene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private CategorieAllergene categorie;

    @Column(length = 500)
    private String description;

    public enum CategorieAllergene {
        CEREALES,          // Gluten
        CRUSTACES,
        OEUFS,
        POISSONS,
        FRUITS_A_COQUE,    // Amandes, noix, noisettes
        LEGUMINEUSES,      // Arachides, soja, lupin
        LAITIER,           // Lactose
        LEGUMES,           // Céleri
        CONDIMENTS,        // Moutarde
        GRAINES,           // Sésame
        ADDITIFS,          // Sulfites
        MOLLUSQUES
    }
}