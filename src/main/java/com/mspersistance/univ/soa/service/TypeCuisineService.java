package com.mspersistance.univ.soa.service;

import com.mspersistance.univ.soa.model.TypeCuisine;
import com.mspersistance.univ.soa.repository.TypeCuisineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TypeCuisineService {

    private final TypeCuisineRepository typeCuisineRepository;

    public List<TypeCuisine> findAll() {
        return typeCuisineRepository.findAll();
    }

    public Optional<TypeCuisine> findById(Long id) {
        return typeCuisineRepository.findById(id);
    }

    public TypeCuisine save(TypeCuisine typeCuisine) {
        return typeCuisineRepository.save(typeCuisine);
    }

    public void deleteById(Long id) {
        typeCuisineRepository.deleteById(id);
    }
}