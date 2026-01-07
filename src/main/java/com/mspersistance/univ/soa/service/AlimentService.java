package com.mspersistance.univ.soa.service;

import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.model.Aliment;
import com.mspersistance.univ.soa.repository.AlimentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des aliments.
 */
@Service
public class AlimentService {

    private final AlimentRepository alimentRepository;

    public AlimentService(AlimentRepository alimentRepository) {
        this.alimentRepository = alimentRepository;
    }

    public List<Aliment> findAll() {
        return alimentRepository.findAll();
    }

    public Optional<Aliment> findById(Long id) {
        return alimentRepository.findById(id);
    }

    public Optional<Aliment> findByNom(String nom) {
        return alimentRepository.findByNomIgnoreCase(nom);
    }

    @Transactional
    public Aliment save(Aliment aliment) {
        aliment.setId(null);
        return alimentRepository.save(aliment);
    }

    @Transactional
    public Aliment update(Long id, Aliment aliment) {
        Aliment existing = alimentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aliment non trouvÃ© avec l'ID: " + id));

        existing.setNom(aliment.getNom());
        existing.setCategorieAliment(aliment.getCategorieAliment());
        existing.setCalories(aliment.getCalories());
        existing.setProteines(aliment.getProteines());
        existing.setGlucides(aliment.getGlucides());
        existing.setLipides(aliment.getLipides());
        existing.setFibres(aliment.getFibres());

        return alimentRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!alimentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Aliment non trouvÃ© avec l'ID: " + id);
        }
        alimentRepository.deleteById(id);
    }
}
