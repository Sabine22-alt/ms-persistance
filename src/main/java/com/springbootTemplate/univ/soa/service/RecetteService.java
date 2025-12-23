package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.dto.RecetteDTO;
import com.springbootTemplate.univ.soa.exception.ResourceNotFoundException;
import com.springbootTemplate.univ.soa.model.*;
import com.springbootTemplate.univ.soa.repository.AlimentRepository;
import com.springbootTemplate.univ.soa.repository.RecetteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecetteService {

    @Autowired
    private RecetteRepository recetteRepository;

    @Autowired
    private AlimentRepository alimentRepository;

    public List<Recette> findAll() {
        return recetteRepository.findAll();
    }

    public Optional<Recette> findById(Long id) {
        return recetteRepository.findById(id);
    }

    public List<Recette> findByStatut(Recette.StatutRecette statut) {
        return recetteRepository.findByStatut(statut);
    }

    @Transactional
    public Recette save(Recette recette) {
        recette.setId(null);

        // Traiter les ingrédients
        if (recette.getIngredients() != null && !recette.getIngredients().isEmpty()) {
            for (Ingredient ingredient : recette.getIngredients()) {
                ingredient.setId(null);

                if (ingredient.getAliment() != null && ingredient.getAliment().getId() != null) {
                    Aliment aliment = alimentRepository.findById(ingredient.getAliment().getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Aliment non trouvé avec l'ID: " + ingredient.getAliment().getId()
                            ));
                    ingredient.setAliment(aliment);
                }
                ingredient.setRecette(recette);
            }
        }

        // Traiter les étapes
        if (recette.getEtapes() != null && !recette.getEtapes().isEmpty()) {
            for (Etape etape : recette.getEtapes()) {
                etape.setId(null);
                etape.setRecette(recette);
            }
        }

        return recetteRepository.save(recette);
    }

    @Transactional
    public Recette update(Long id, Recette recette) {
        Recette existing = recetteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + id));

        // Mise à jour des champs de base
        existing.setTitre(recette.getTitre());
        existing.setDescription(recette.getDescription());
        existing.setTempsTotal(recette.getTempsTotal());
        existing.setKcal(recette.getKcal());
        existing.setImageUrl(recette.getImageUrl());
        existing.setDifficulte(recette.getDifficulte());
        existing.setActif(recette.getActif());
        existing.setStatut(recette.getStatut());
        existing.setMotifRejet(recette.getMotifRejet());

        // Mise à jour des ingrédients
        if (recette.getIngredients() != null) {
            existing.getIngredients().clear();

            for (Ingredient ingredient : recette.getIngredients()) {
                ingredient.setId(null);

                if (ingredient.getAliment() != null && ingredient.getAliment().getId() != null) {
                    Aliment aliment = alimentRepository.findById(ingredient.getAliment().getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Aliment non trouvé avec l'ID: " + ingredient.getAliment().getId()
                            ));
                    ingredient.setAliment(aliment);
                    ingredient.setRecette(existing);
                    existing.getIngredients().add(ingredient);
                }
            }
        }

        // Mise à jour des étapes
        if (recette.getEtapes() != null) {
            existing.getEtapes().clear();

            for (Etape etape : recette.getEtapes()) {
                etape.setId(null);
                etape.setRecette(existing);
                existing.getEtapes().add(etape);
            }
        }

        return recetteRepository.save(existing);
    }

    /**
     * Méthode pour créer une recette depuis un RecetteDTO
     */
    @Transactional
    public Recette saveFromDTO(RecetteDTO dto) {
        Recette recette = new Recette();
        recette.setTitre(dto.getTitre());
        recette.setDescription(dto.getDescription());
        recette.setTempsTotal(dto.getTempsTotal());
        recette.setKcal(dto.getKcal());
        recette.setImageUrl(dto.getImageUrl());
        recette.setDifficulte(dto.getDifficulte());
        // par défaut: actif=false, statut=EN_ATTENTE
        recette.setActif(Boolean.FALSE);
        recette.setStatut(Recette.StatutRecette.EN_ATTENTE);
        recette.setMotifRejet(null);

        // Traiter les ingrédients depuis le DTO
        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            for (RecetteDTO.IngredientDTO ingredientDTO : dto.getIngredients()) {
                Ingredient ingredient = new Ingredient();

                // Récupérer l'aliment
                Aliment aliment = alimentRepository.findById(ingredientDTO.getAlimentId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Aliment non trouvé avec l'ID: " + ingredientDTO.getAlimentId()
                        ));

                ingredient.setAliment(aliment);
                ingredient.setQuantite(ingredientDTO.getQuantite());
                ingredient.setUnite(ingredientDTO.getUnite() != null ?
                        Ingredient.Unite.valueOf(ingredientDTO.getUnite()) : null);
                ingredient.setPrincipal(ingredientDTO.getPrincipal());
                ingredient.setRecette(recette);

                recette.getIngredients().add(ingredient);
            }
        }

        // Traiter les étapes depuis le DTO
        if (dto.getEtapes() != null && !dto.getEtapes().isEmpty()) {
            for (RecetteDTO.EtapeDTO etapeDTO : dto.getEtapes()) {
                Etape etape = new Etape();
                etape.setOrdre(etapeDTO.getOrdre());
                etape.setTemps(etapeDTO.getTemps());
                etape.setTexte(etapeDTO.getTexte());
                etape.setRecette(recette);

                recette.getEtapes().add(etape);
            }
        }

        return recetteRepository.save(recette);
    }

    /**
     * Méthode pour mettre à jour une recette depuis un RecetteDTO
     */
    @Transactional
    public Recette updateFromDTO(Long id, RecetteDTO dto) {
        Recette existing = recetteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + id));

        // Mise à jour des champs de base
        existing.setTitre(dto.getTitre());
        existing.setDescription(dto.getDescription());
        existing.setTempsTotal(dto.getTempsTotal());
        existing.setKcal(dto.getKcal());
        existing.setImageUrl(dto.getImageUrl());
        existing.setDifficulte(dto.getDifficulte());
        // ne pas changer actif/statut/motif ici via DTO utilisateur standard

        // Mise à jour des ingrédients
        existing.getIngredients().clear();

        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            for (RecetteDTO.IngredientDTO ingredientDTO : dto.getIngredients()) {
                Ingredient ingredient = new Ingredient();

                // Récupérer l'aliment
                Aliment aliment = alimentRepository.findById(ingredientDTO.getAlimentId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Aliment non trouvé avec l'ID: " + ingredientDTO.getAlimentId()
                        ));

                ingredient.setAliment(aliment);
                ingredient.setQuantite(ingredientDTO.getQuantite());
                ingredient.setUnite(ingredientDTO.getUnite() != null ?
                        Ingredient.Unite.valueOf(ingredientDTO.getUnite()) : null);
                ingredient.setPrincipal(ingredientDTO.getPrincipal());
                ingredient.setRecette(existing);

                existing.getIngredients().add(ingredient);
            }
        }

        // Mise à jour des étapes
        existing.getEtapes().clear();

        if (dto.getEtapes() != null && !dto.getEtapes().isEmpty()) {
            for (RecetteDTO.EtapeDTO etapeDTO : dto.getEtapes()) {
                Etape etape = new Etape();
                etape.setOrdre(etapeDTO.getOrdre());
                etape.setTemps(etapeDTO.getTemps());
                etape.setTexte(etapeDTO.getTexte());
                etape.setRecette(existing);

                existing.getEtapes().add(etape);
            }
        }

        return recetteRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!recetteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recette non trouvée avec l'ID: " + id);
        }
        recetteRepository.deleteById(id);
    }

    /**
     * Valider une recette (passer à VALIDEE et actif=true)
     */
    @Transactional
    public Recette validerRecette(Long id) {
        Recette recette = recetteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + id));

        recette.setActif(true);
        recette.setStatut(Recette.StatutRecette.VALIDEE);
        recette.setMotifRejet(null);

        return recetteRepository.save(recette);
    }

    /**
     * Rejeter une recette (passer à REJETEE avec motif)
     */
    @Transactional
    public Recette rejeterRecette(Long id, String motif) {
        Recette recette = recetteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + id));

        recette.setActif(false);
        recette.setStatut(Recette.StatutRecette.REJETEE);
        recette.setMotifRejet(motif);

        return recetteRepository.save(recette);
    }
}