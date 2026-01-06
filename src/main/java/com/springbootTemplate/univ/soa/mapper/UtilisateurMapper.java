package com.springbootTemplate.univ.soa.mapper;

import com.springbootTemplate.univ.soa.dto.UtilisateurDTO;
import com.springbootTemplate.univ.soa.model.Allergene;
import com.springbootTemplate.univ.soa.model.RegimeAlimentaire;
import com.springbootTemplate.univ.soa.model.TypeCuisine;
import com.springbootTemplate.univ.soa.model.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UtilisateurMapper {

    public UtilisateurDTO toDTO(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(utilisateur.getId());
        dto.setEmail(utilisateur.getEmail());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setActif(utilisateur.getActif());
        dto.setRole(utilisateur.getRole());
        dto.setDateCreation(utilisateur.getDateCreation());
        dto.setDateModification(utilisateur.getDateModification());

        // ✅ MAPPER LES RÉGIMES ALIMENTAIRES
        if (utilisateur.getRegimes() != null) {
            dto.setRegimesIds(
                    utilisateur.getRegimes().stream()
                            .map(RegimeAlimentaire::getId)
                            .collect(Collectors.toSet())
            );
        }

        // ✅ MAPPER LES ALLERGÈNES
        if (utilisateur.getAllergenes() != null) {
            dto.setAllergenesIds(
                    utilisateur.getAllergenes().stream()
                            .map(Allergene::getId)
                            .collect(Collectors.toSet())
            );
        }

        // ✅ MAPPER LES TYPES DE CUISINE
        if (utilisateur.getTypesCuisinePreferes() != null) {
            dto.setTypesCuisinePreferesIds(
                    utilisateur.getTypesCuisinePreferes().stream()
                            .map(TypeCuisine::getId)
                            .collect(Collectors.toSet())
            );
        }

        return dto;
    }

    public Utilisateur toEntity(UtilisateurDTO dto) {
        if (dto == null) {
            return null;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(dto.getId());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMotDePasse(dto.getMotDePasse());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setActif(dto.getActif());
        utilisateur.setRole(dto.getRole());

        // Note : Les relations ManyToMany (regimes, allergenes, typesCuisinePreferes)
        // sont gérées dans le service via les repositories
        // On ne les mappe pas ici pour éviter les problèmes de cascade

        return utilisateur;
    }
}