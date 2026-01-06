package com.mspersistance.univ.soa.repository;

import com.mspersistance.univ.soa.model.Aliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlimentRepository extends JpaRepository<Aliment, Long> {
    Optional<Aliment> findByNomIgnoreCase(String nom);
}