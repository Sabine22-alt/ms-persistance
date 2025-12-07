package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.FichierRecette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichierRecetteRepository extends JpaRepository<FichierRecette, Long> {
    List<FichierRecette> findByRecetteId(Long recetteId);
    List<FichierRecette> findByRecetteIdAndType(Long recetteId, FichierRecette.TypeFichier type);
    void deleteByRecetteId(Long recetteId);
}

