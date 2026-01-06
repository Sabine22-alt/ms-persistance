package com.mspersistance.univ.soa.mapper;

import com.mspersistance.univ.soa.dto.UtilisateurDTO;
import com.mspersistance.univ.soa.model.Aliment;
import com.mspersistance.univ.soa.model.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre Utilisateur et UtilisateurDTO (Record).
 * Note: Les Records utilisent des accesseurs directs (email() au lieu de getEmail())
 * et des constructeurs complets au lieu de setters.
 */
@Component
public class UtilisateurMapper {

    /**
     * Convertit une entité Utilisateur en DTO Record
     */
    public UtilisateurDTO toDTO(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        return new UtilisateurDTO(
                utilisateur.getId(),
                utilisateur.getEmail(),
                null, // Ne jamais retourner le mot de passe
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getTelephone(),
                utilisateur.getBio(),
                utilisateur.getAdresse(),
                utilisateur.getActif(),
                utilisateur.getRole(),
                utilisateur.getAlimentsExclus() != null
                        ? utilisateur.getAlimentsExclus().stream()
                                .map(Aliment::getId)
                                .collect(Collectors.toSet())
                        : null,
                utilisateur.getDateCreation(),
                utilisateur.getDateModification()
        );
    }

    /**
     * Convertit un DTO Record en entité Utilisateur
     */
    public Utilisateur toEntity(UtilisateurDTO dto) {
        if (dto == null) {
            return null;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(dto.id());
        utilisateur.setEmail(dto.email());
        utilisateur.setMotDePasse(dto.motDePasse());
        utilisateur.setNom(dto.nom());
        utilisateur.setPrenom(dto.prenom());
        utilisateur.setTelephone(dto.telephone());
        utilisateur.setBio(dto.bio());
        utilisateur.setAdresse(dto.adresse());
        utilisateur.setActif(dto.actif());
        utilisateur.setRole(dto.role());

        return utilisateur;
    }
}