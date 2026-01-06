package com.springbootTemplate.univ.soa.dto;

import com.springbootTemplate.univ.soa.model.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurDTO {
    private Long id;
    private String email;
    private String motDePasse;
    private String nom;
    private String prenom;
    private Boolean actif;
    private Utilisateur.Role role;
    private Set<Long> regimesIds;
    private Set<Long> allergenesIds;
    private Set<Long> typesCuisinePreferesIds;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}