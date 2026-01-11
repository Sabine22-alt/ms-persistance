package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.Favori;
import com.springbootTemplate.univ.soa.model.Recette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriRepository extends JpaRepository<Favori, Long> {

    boolean existsByUtilisateurIdAndRecetteId(Long utilisateurId, Long recetteId);

    Optional<Favori> findByUtilisateurIdAndRecetteId(Long utilisateurId, Long recetteId);


    @Query("SELECT f.recette FROM Favori f WHERE f.utilisateurId = :utilisateurId ORDER BY f.dateAjout DESC")
    List<Recette> findRecettesFavoritesByUtilisateurId(@Param("utilisateurId") Long utilisateurId);

    long countByRecetteId(Long recetteId);

}