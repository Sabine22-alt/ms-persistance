package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recettes", indexes = {
    @Index(name = "idx_recettes_utilisateur_id", columnList = "utilisateur_id"),
    @Index(name = "idx_recettes_statut", columnList = "statut"),
    @Index(name = "idx_recettes_date_creation", columnList = "date_creation")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    // Ajout description
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "temps_total")
    private Integer tempsTotal; // en minutes

    @Column
    private Integer kcal;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Difficulte difficulte;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(nullable = false)
    private Boolean actif = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutRecette statut = StatutRecette.EN_ATTENTE;

    @Column(name = "motif_rejet", length = 500)
    private String motifRejet;

    @Column(name = "utilisateur_id")
    private Long utilisateurId;

    @Column(name = "moyenne_evaluation")
    private Double moyenneEvaluation = 0.0; // Dénormalisée pour perf

    @OneToMany(mappedBy = "recette", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Ingredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recette", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordre ASC")
    private List<Etape> etapes = new ArrayList<>();

    @OneToMany(mappedBy = "recette", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "recette", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FichierRecette> fichiers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public enum Difficulte {
        FACILE,
        MOYEN,
        DIFFICILE
    }

    public enum StatutRecette {
        EN_ATTENTE,
        VALIDEE,
        REJETEE
    }
}