package com.springbootTemplate.univ.soa.repository;

import com.springbootTemplate.univ.soa.model.TypeCuisine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeCuisineRepository extends JpaRepository<TypeCuisine, Long> {
    Optional<TypeCuisine> findByNom(String nom);
}