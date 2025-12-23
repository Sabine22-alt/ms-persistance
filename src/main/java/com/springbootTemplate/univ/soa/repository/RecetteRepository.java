package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.Recette;
import com.springbootTemplate.univ.soa.model.Recette.StatutRecette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecetteRepository extends JpaRepository<Recette, Long> {
    List<Recette> findByStatut(StatutRecette statut);
}