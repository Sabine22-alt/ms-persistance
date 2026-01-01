package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.Activite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Long> {

    @Query("SELECT a FROM Activite a WHERE a.utilisateurId = :utilisateurId ORDER BY a.dateActivite DESC")
    List<Activite> findByUtilisateurIdOrderByDateActivityDesc(@Param("utilisateurId") Long utilisateurId);

    @Query("SELECT a FROM Activite a WHERE a.utilisateurId = :utilisateurId ORDER BY a.dateActivite DESC LIMIT 10")
    List<Activite> findLast10ActivitiesByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
}

