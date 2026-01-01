package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks", indexes = {
    @Index(name = "idx_feedbacks_recette_id", columnList = "recette_id"),
    @Index(name = "idx_feedbacks_recette_evaluation", columnList = "recette_id,evaluation")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, foreignKey = @ForeignKey(name = "fk_feedback_utilisateur"))
    @JsonBackReference
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recette_id", nullable = false, foreignKey = @ForeignKey(name = "fk_feedback_recette"))
    @JsonBackReference
    private Recette recette;

    @Column(nullable = false)
    private Integer evaluation;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_feedback")
    private LocalDateTime dateFeedback;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateFeedback = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}