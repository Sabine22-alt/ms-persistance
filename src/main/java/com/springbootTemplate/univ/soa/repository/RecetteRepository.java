package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.Recette;
import com.springbootTemplate.univ.soa.model.Recette.StatutRecette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecetteRepository extends JpaRepository<Recette, Long> {

    @EntityGraph(attributePaths = {"ingredients", "etapes", "feedbacks"})
    @Query("SELECT DISTINCT r FROM Recette r ORDER BY r.dateCreation DESC")
    List<Recette> findAllOptimized();

    @EntityGraph(attributePaths = {"ingredients", "etapes", "feedbacks"})
    @Query("SELECT DISTINCT r FROM Recette r WHERE r.statut = :statut ORDER BY r.dateCreation DESC")
    List<Recette> findByStatutOptimized(@Param("statut") StatutRecette statut);

    @EntityGraph(attributePaths = {"ingredients", "etapes", "feedbacks"})
    @Query("SELECT DISTINCT r FROM Recette r WHERE r.utilisateurId = :utilisateurId ORDER BY r.dateCreation DESC")
    List<Recette> findByUtilisateurIdOptimized(@Param("utilisateurId") Long utilisateurId);

    @EntityGraph(attributePaths = {"ingredients", "etapes", "feedbacks"})
    @Query("SELECT r FROM Recette r WHERE r.id = :id")
    Optional<Recette> findByIdOptimized(@Param("id") Long id);

    // Méthodes originales pour compatibilité
    List<Recette> findByStatut(StatutRecette statut);
    List<Recette> findByUtilisateurId(Long utilisateurId);
    List<Recette> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);
}