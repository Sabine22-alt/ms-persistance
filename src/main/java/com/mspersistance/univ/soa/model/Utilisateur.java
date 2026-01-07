package com.mspersistance.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(length = 100)
    private String nom;

    @Column(length = 100)
    private String prenom;

    @Column(length = 20, nullable = true)
    private String telephone; // optionnel

    @Column(length = 500, nullable = true)
    private String bio; // optionnel

    @Column(length = 500, nullable = true)
    private String adresse; // optionnel

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Role role = Role.USER;

    // ALIMENTS EXCLUS
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "utilisateur_aliments_exclus",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "aliment_id")
    )
    @Builder.Default
    private Set<Aliment> alimentsExclus = new HashSet<>();

    // RÉGIMES ALIMENTAIRES
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "utilisateur_regimes",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "regime_id")
    )
    @Builder.Default
    private Set<RegimeAlimentaire> regimesAlimentaires = new HashSet<>();

    // ALLERGÈNES
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "utilisateur_allergenes",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "allergene_id")
    )
    @Builder.Default
    private Set<Allergene> allergenes = new HashSet<>();

    // TYPES DE CUISINE PRÉFÉRÉS
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "utilisateur_types_cuisine",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "type_cuisine_id")
    )
    @Builder.Default
    private Set<TypeCuisine> typesCuisinePreferences = new HashSet<>();

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (actif == null) {
            actif = true;
        }
        if (role == null) {
            role = Role.USER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public enum Role {
        USER,
        ADMIN
    }
}

