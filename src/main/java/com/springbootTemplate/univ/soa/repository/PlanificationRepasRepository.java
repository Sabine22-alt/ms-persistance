package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.PlanificationRepas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanificationRepasRepository extends JpaRepository<PlanificationRepas, Long> {

    @EntityGraph(attributePaths = {"jours", "jours.repas"})
    @Query("SELECT p FROM PlanificationRepas p WHERE p.utilisateurId = :utilisateurId AND p.semaine = :semaine AND p.annee = :annee")
    Optional<PlanificationRepas> findByUtilisateurAndWeek(
        @Param("utilisateurId") Long utilisateurId,
        @Param("semaine") Integer semaine,
        @Param("annee") Integer annee
    );

    @Query("SELECT p FROM PlanificationRepas p WHERE p.utilisateurId = :utilisateurId ORDER BY p.annee DESC, p.semaine DESC")
    List<PlanificationRepas> findByUtilisateurIdOrderByWeekDesc(@Param("utilisateurId") Long utilisateurId);
}

