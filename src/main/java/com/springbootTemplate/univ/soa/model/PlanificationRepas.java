package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planifications_repas", indexes = {
    @Index(name = "idx_plan_utilisateur", columnList = "utilisateur_id"),
    @Index(name = "idx_plan_semaine", columnList = "utilisateur_id,semaine,annee")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanificationRepas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", nullable = false)
    private Long utilisateurId;

    @Column(nullable = false)
    private Integer semaine; // 1-53

    @Column(nullable = false)
    private Integer annee;

    @OneToMany(mappedBy = "planification", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<PlanificationJour> jours = new ArrayList<>();

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}

