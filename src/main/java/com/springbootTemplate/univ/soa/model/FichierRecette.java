package com.springbootTemplate.univ.soa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fichiers_recette", indexes = {
    @Index(name = "idx_fichiers_recette_id", columnList = "recette_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FichierRecette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String nomOriginal;

    @Column(nullable = false, length = 500)
    private String nomStocke;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long taille; // en bytes

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeFichier type;

    @Column(length = 500)
    private String cheminMinio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recette_id", nullable = false, foreignKey = @ForeignKey(name = "fk_fichier_recette"))
    private Recette recette;

    @Column(name = "date_upload", updatable = false)
    private LocalDateTime dateUpload;

    @PrePersist
    protected void onCreate() {
        dateUpload = LocalDateTime.now();
    }

    public enum TypeFichier {
        IMAGE,
        DOCUMENT
    }
}

