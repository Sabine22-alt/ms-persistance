package com.mspersistance.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "repas_planifies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepasPlannifie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planification_jour_id", nullable = false)
    @JsonBackReference("jour-repas")
    private PlanificationJour planificationJour;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private TypeRepas typeRepas; // 0=petit-dÃ©j, 1=dÃ©jeuner, 2=dÃ®ner

    @Column(name = "recette_id")
    private Long recetteId; // Nullable, rÃ©fÃ©rence Ã  une recette

    @Column(columnDefinition = "TEXT")
    private String noteLibre; // Description manuelle si pas de recette

    public enum TypeRepas {
        PETIT_DEJEUNER(0, "Petit-dÃ©jeuner"),
        DEJEUNER(1, "DÃ©jeuner"),
        DINER(2, "DÃ®ner");

        private final int value;
        private final String label;

        TypeRepas(int value, String label) {
            this.value = value;
            this.label = label;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }
}

