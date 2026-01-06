package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.Allergene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllergeneRepository extends JpaRepository<Allergene, Long> {
    Optional<Allergene> findByNom(String nom);
    List<Allergene> findByCategorie(Allergene.CategorieAllergene categorie);
}