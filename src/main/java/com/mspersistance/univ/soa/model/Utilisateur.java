package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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

    @Column(nullable = false)
    private Boolean actif = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role = Role.USER;

    // RÉGIMES ALIMENTAIRES
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "utilisateur_regimes",
            joinColumns = @JoinColumn(name = "utilisateur_id", foreignKey = @ForeignKey(name = "fk_user_regimes")),
            inverseJoinColumns = @JoinColumn(name = "regime_id", foreignKey = @ForeignKey(name = "fk_regime_user"))
    )
    private Set<RegimeAlimentaire> regimes = new HashSet<>();

    // ALLERGÈNES
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "utilisateur_allergenes",
            joinColumns = @JoinColumn(name = "utilisateur_id", foreignKey = @ForeignKey(name = "fk_user_allergenes")),
            inverseJoinColumns = @JoinColumn(name = "allergene_id", foreignKey = @ForeignKey(name = "fk_allergene_user"))
    )
    private Set<Allergene> allergenes = new HashSet<>();

    // TYPES DE CUISINE PRÉFÉRÉS
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "utilisateur_cuisines",
            joinColumns = @JoinColumn(name = "utilisateur_id", foreignKey = @ForeignKey(name = "fk_user_cuisines")),
            inverseJoinColumns = @JoinColumn(name = "type_cuisine_id", foreignKey = @ForeignKey(name = "fk_cuisine_user"))
    )
    private Set<TypeCuisine> typesCuisinePreferes = new HashSet<>();

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