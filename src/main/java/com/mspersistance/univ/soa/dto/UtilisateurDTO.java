package com.mspersistance.univ.soa.dto;

import com.mspersistance.univ.soa.model.Utilisateur;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO Utilisateur en Record Java (Java 16+)
 *
 * Avantages des Records:
 * - Immutable par dÃ©faut (thread-safe)
 * - Pas de boilerplate (equals, hashCode, toString auto)
 * - Performance optimale (compact object layout)
 * - SÃ©mantique claire (data carrier)
 *
 * Fusion des champs de la version main + optimisation
 */
public record UtilisateurDTO(
    Long id,
    String email,
    String motDePasse,
    String nom,
    String prenom,
    String telephone,
    String bio,
    String adresse,
    Boolean actif,
    Utilisateur.Role role,
    Set<Long> regimesIds,
    Set<Long> allergenesIds,
    Set<Long> typesCuisinePreferesIds,
    Set<Long> alimentsExclusIds,
    LocalDateTime dateCreation,
    LocalDateTime dateModification
) {
    /**
     * Compact Constructor - Validation
     */
    public UtilisateurDTO {
        // Validation peut Ãªtre ajoutÃ©e ici si nÃ©cessaire
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }

    /**
     * Builder statique pour faciliter la crÃ©ation
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String motDePasse;
        private String nom;
        private String prenom;
        private String telephone;
        private String bio;
        private String adresse;
        private Boolean actif;
        private Utilisateur.Role role;
        private Set<Long> regimesIds;
        private Set<Long> allergenesIds;
        private Set<Long> typesCuisinePreferesIds;
        private Set<Long> alimentsExclusIds;
        private LocalDateTime dateCreation;
        private LocalDateTime dateModification;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder motDePasse(String motDePasse) { this.motDePasse = motDePasse; return this; }
        public Builder nom(String nom) { this.nom = nom; return this; }
        public Builder prenom(String prenom) { this.prenom = prenom; return this; }
        public Builder telephone(String telephone) { this.telephone = telephone; return this; }
        public Builder bio(String bio) { this.bio = bio; return this; }
        public Builder adresse(String adresse) { this.adresse = adresse; return this; }
        public Builder actif(Boolean actif) { this.actif = actif; return this; }
        public Builder role(Utilisateur.Role role) { this.role = role; return this; }
        public Builder regimesIds(Set<Long> regimesIds) { this.regimesIds = regimesIds; return this; }
        public Builder allergenesIds(Set<Long> allergenesIds) { this.allergenesIds = allergenesIds; return this; }
        public Builder typesCuisinePreferesIds(Set<Long> typesCuisinePreferesIds) { this.typesCuisinePreferesIds = typesCuisinePreferesIds; return this; }
        public Builder alimentsExclusIds(Set<Long> alimentsExclusIds) { this.alimentsExclusIds = alimentsExclusIds; return this; }
        public Builder dateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; return this; }
        public Builder dateModification(LocalDateTime dateModification) { this.dateModification = dateModification; return this; }

        public UtilisateurDTO build() {
            return new UtilisateurDTO(id, email, motDePasse, nom, prenom, telephone, bio,
                adresse, actif, role, regimesIds, allergenesIds, typesCuisinePreferesIds,
                alimentsExclusIds, dateCreation, dateModification);
        }
    }
}
