package com.mspersistance.univ.soa.factory;

import com.mspersistance.univ.soa.dto.UtilisateurDTO;
import com.mspersistance.univ.soa.model.Aliment;
import com.mspersistance.univ.soa.model.Utilisateur;
import com.mspersistance.univ.soa.repository.AlimentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Factory pour créer des entités Utilisateur.
 * Design Pattern: Factory Method
 *
 * Avantages:
 * - Centralisation de la logique de création
 * - Réduction de la duplication de code
 * - Facilite les tests unitaires
 * - Code plus maintenable et lisible
 */
@Component
public class UtilisateurFactory {

    private final PasswordEncoder passwordEncoder;
    private final AlimentRepository alimentRepository;

    public UtilisateurFactory(PasswordEncoder passwordEncoder, AlimentRepository alimentRepository) {
        this.passwordEncoder = passwordEncoder;
        this.alimentRepository = alimentRepository;
    }

    /**
     * Crée un nouvel utilisateur à partir d'un DTO Record.
     * Pattern: Factory Method + Builder
     *
     * Note: Avec Records, on utilise les accesseurs directs (email() au lieu de getEmail())
     */
    public Utilisateur createFromDTO(UtilisateurDTO dto) {
        Set<Aliment> alimentsExclus = buildAlimentsExclus(dto.alimentsExclusIds());

        return Utilisateur.builder()
                .email(dto.email())
                .motDePasse(encodePasswordIfPresent(dto.motDePasse()))
                .nom(dto.nom())
                .prenom(dto.prenom())
                // Champs optionnels uniquement si fournis (sinon laissés à null)
                .telephone(dto.telephone())
                .bio(dto.bio())
                .adresse(dto.adresse())
                .alimentsExclus(alimentsExclus)
                .actif(dto.actif() != null ? dto.actif() : true)
                .role(dto.role() != null ? dto.role() : Utilisateur.Role.USER)
                .build();
    }

    /**
     * Met à jour un utilisateur existant avec les données du DTO Record.
     * Pattern: Builder + Fluent Interface
     */
    public Utilisateur updateFromDTO(Utilisateur existing, UtilisateurDTO dto) {
        existing.setEmail(dto.email());
        if (dto.motDePasse() != null) {
            existing.setMotDePasse(passwordEncoder.encode(dto.motDePasse()));
        }
        existing.setNom(dto.nom());
        existing.setPrenom(dto.prenom());
        if (dto.telephone() != null) existing.setTelephone(dto.telephone());
        if (dto.bio() != null) existing.setBio(dto.bio());
        if (dto.adresse() != null) existing.setAdresse(dto.adresse());
        if (dto.actif() != null) existing.setActif(dto.actif());
        if (dto.role() != null) existing.setRole(dto.role());

        // Mise à jour des aliments exclus
        if (dto.alimentsExclusIds() != null) {
            existing.getAlimentsExclus().clear();
            existing.getAlimentsExclus().addAll(buildAlimentsExclus(dto.alimentsExclusIds()));
        }

        return existing;
    }

    /**
     * Encode le mot de passe si présent.
     */
    private String encodePasswordIfPresent(String password) {
        return (password != null && !password.isEmpty())
                ? passwordEncoder.encode(password)
                : null;
    }

    /**
     * Construit le set d'aliments exclus à partir des IDs.
     */
    private Set<Aliment> buildAlimentsExclus(Set<Long> alimentIds) {
        if (alimentIds == null || alimentIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Aliment> aliments = new HashSet<>();
        for (Long alimentId : alimentIds) {
            alimentRepository.findById(alimentId).ifPresent(aliments::add);
        }
        return aliments;
    }

    /**
     * Crée un utilisateur simple (pour les tests).
     */
    public Utilisateur createSimple(String email, String password, String nom, String prenom) {
        return Utilisateur.builder()
                .email(email)
                .motDePasse(passwordEncoder.encode(password))
                .nom(nom)
                .prenom(prenom)
                .actif(true)
                .role(Utilisateur.Role.USER)
                .alimentsExclus(new HashSet<>())
                .build();
    }
}
