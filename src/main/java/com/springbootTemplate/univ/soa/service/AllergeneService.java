package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.model.Allergene;
import com.springbootTemplate.univ.soa.repository.AllergeneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AllergeneService {

    private final AllergeneRepository allergeneRepository;

    public List<Allergene> findAll() {
        return allergeneRepository.findAll();
    }

    public Optional<Allergene> findById(Long id) {
        return allergeneRepository.findById(id);
    }

    public Allergene save(Allergene allergene) {
        return allergeneRepository.save(allergene);
    }

    public void deleteById(Long id) {
        allergeneRepository.deleteById(id);
    }
}