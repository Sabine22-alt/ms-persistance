package com.mspersistance.univ.soa.repository;

import com.mspersistance.univ.soa.model.RegimeAlimentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegimeAlimentaireRepository extends JpaRepository<RegimeAlimentaire, Long> {
    Optional<RegimeAlimentaire> findByNom(String nom);
}