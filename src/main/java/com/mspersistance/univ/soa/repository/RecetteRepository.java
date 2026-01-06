package com.mspersistance.univ.soa.repository;

import com.mspersistance.univ.soa.model.Recette;
import com.mspersistance.univ.soa.model.Recette.StatutRecette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecetteRepository extends JpaRepository<Recette, Long> {

    /**
     * Récupère toutes les recettes SANS collections (ultra rapide, pas de N+1)
     * Les collections seront chargées uniquement si nécessaire via DTO projection
     */
    @Query("SELECT r FROM Recette r ORDER BY r.dateCreation DESC")
    List<Recette> findAllOptimized();

    @Query("SELECT r FROM Recette r WHERE r.statut = :statut ORDER BY r.dateCreation DESC")
    List<Recette> findByStatutOptimized(@Param("statut") StatutRecette statut);

    @Query("SELECT r FROM Recette r WHERE r.utilisateurId = :utilisateurId ORDER BY r.dateCreation DESC")
    List<Recette> findByUtilisateurIdOptimized(@Param("utilisateurId") Long utilisateurId);

    /**
     * Récupère une recette par ID avec TOUTES ses collections en 3 queries (pas N+1)
     * Query 1: Recette + ingredients (JOIN FETCH)
     * Query 2: etapes (SUBSELECT)
     * Query 3: fichiers (SUBSELECT)
     */
    @Query("SELECT DISTINCT r FROM Recette r " +
           "LEFT JOIN FETCH r.ingredients i " +
           "LEFT JOIN FETCH i.aliment " +
           "WHERE r.id = :id")
    Optional<Recette> findByIdOptimized(@Param("id") Long id);

    /**
     * Récupère une recette par ID SANS les collections lazy
     * Utilisé dans les services pour les opérations d'écriture où on n'a besoin que de la recette de base
     */
    @Query("SELECT r FROM Recette r WHERE r.id = :id")
    Optional<Recette> findByIdSimple(@Param("id") Long id);

    // Méthodes originales pour compatibilité
    List<Recette> findByStatut(StatutRecette statut);
    List<Recette> findByUtilisateurId(Long utilisateurId);
    List<Recette> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);
}