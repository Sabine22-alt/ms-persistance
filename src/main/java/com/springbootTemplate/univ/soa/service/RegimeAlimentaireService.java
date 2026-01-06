package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.model.RegimeAlimentaire;
import com.springbootTemplate.univ.soa.repository.RegimeAlimentaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegimeAlimentaireService {

    private final RegimeAlimentaireRepository regimeAlimentaireRepository;

    public List<RegimeAlimentaire> findAll() {
        return regimeAlimentaireRepository.findAll();
    }

    public Optional<RegimeAlimentaire> findById(Long id) {
        return regimeAlimentaireRepository.findById(id);
    }

    public RegimeAlimentaire save(RegimeAlimentaire regime) {
        return regimeAlimentaireRepository.save(regime);
    }

    public void deleteById(Long id) {
        regimeAlimentaireRepository.deleteById(id);
    }
}