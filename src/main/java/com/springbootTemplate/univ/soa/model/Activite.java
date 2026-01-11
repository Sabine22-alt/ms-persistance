package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activites", indexes = {
    @Index(name = "idx_activites_utilisateur", columnList = "utilisateur_id"),
    @Index(name = "idx_activites_date", columnList = "date_activite DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", nullable = false)
    private Long utilisateurId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TypeActivite type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_activite", nullable = false)
    private LocalDateTime dateActivite;

    @Column(columnDefinition = "JSON")
    private String details; // JSON optionnel pour plus d'infos

    @PrePersist
    protected void onCreate() {
        dateActivite = LocalDateTime.now();
    }

    public enum TypeActivite {
        RECETTE_CREEE,
        RECETTE_VALIDEE,
        RECETTE_REJETEE,
        RECETTE_MODIFIEE,
        FEEDBACK_AJOUT,
        FEEDBACK_MODIFIE,
        FEEDBACK_SUPPRIME,
        REPAS_PLANIFIE,
        REPAS_MODIFIE,
        REPAS_SUPPRIME,
        FAVORI_AJOUT,
        FAVORI_SUPPRIME,
        UTILISATEUR_INSCRIT,
        PROFIL_MODIFIE,
        FAVORI_AJOUTE,
        FAVORI_RETIRE
    }
}

