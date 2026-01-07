package com.mspersistance.univ.soa.repository;

import com.mspersistance.univ.soa.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUtilisateur_Id(Long utilisateurId);
    List<Feedback> findByRecette_Id(Long recetteId);

    /**
     * Calcule la moyenne d'Ã©valuation pour une recette spÃ©cifique
     * Retourne Optional.empty() si aucun feedback n'existe
     */
    @Query("SELECT AVG(f.evaluation) FROM Feedback f WHERE f.recette.id = :recetteId")
    Optional<Double> calculateAverageEvaluationByRecetteId(@Param("recetteId") Long recetteId);

    /**
     * Compte le nombre de feedbacks pour une recette
     */
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.recette.id = :recetteId")
    Long countByRecetteId(@Param("recetteId") Long recetteId);

    /**
     * VÃ©rifie si un feedback existe dÃ©jÃ  pour un utilisateur et une recette
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Feedback f WHERE f.utilisateur.id = :utilisateurId AND f.recette.id = :recetteId")
    boolean existsByUtilisateurIdAndRecetteId(@Param("utilisateurId") Long utilisateurId, @Param("recetteId") Long recetteId);
}
