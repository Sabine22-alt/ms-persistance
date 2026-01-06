package com.mspersistance.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
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
        // Recettes
        RECETTE_CREEE,
        RECETTE_MODIFIEE,
        RECETTE_SUPPRIMEE,
        RECETTE_VALIDEE,
        RECETTE_REJETEE,
        RECETTE_PARTAGEE,

        // Feedbacks/Avis
        FEEDBACK_AJOUT,
        FEEDBACK_MODIFIE,
        FEEDBACK_SUPPRIME,

        // Planification de repas
        PLANIFICATION_AJOUT,
        PLANIFICATION_MODIFIE,
        PLANIFICATION_SUPPRIME,
        REPAS_PLANIFIE,        // Alias pour PLANIFICATION_AJOUT (compatibilité)
        REPAS_MODIFIE,         // Alias pour PLANIFICATION_MODIFIE
        REPAS_SUPPRIME,        // Alias pour PLANIFICATION_SUPPRIME

        // Favoris
        FAVORI_AJOUT,
        FAVORI_SUPPRIME,

        // Profil utilisateur
        UTILISATEUR_INSCRIT,
        PROFIL_MODIFIE,
        MOT_DE_PASSE_CHANGE,

        // Aliments
        ALIMENT_EXCLU_AJOUT,
        ALIMENT_EXCLU_SUPPRIME,

        // Notifications
        NOTIFICATION_LUE,
        NOTIFICATION_SUPPRIMEE
    }
}

