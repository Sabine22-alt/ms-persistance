package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.dto.UtilisateurDTO;
import com.springbootTemplate.univ.soa.exception.ResourceNotFoundException;
import com.springbootTemplate.univ.soa.model.*;
import com.springbootTemplate.univ.soa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    // ✅ NOUVEAUX REPOSITORIES
    @Autowired
    private RegimeAlimentaireRepository regimeAlimentaireRepository;

    @Autowired
    private AllergeneRepository allergeneRepository;

    @Autowired
    private TypeCuisineRepository typeCuisineRepository;

    // ===============================
    // OPÉRATIONS CRUD DE BASE
    // ===============================

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Optional<Utilisateur> findById(Long id) {
        return utilisateurRepository.findById(id);
    }

    public Optional<Utilisateur> findByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    @Transactional
    public Utilisateur save(Utilisateur utilisateur) {
        utilisateur.setId(null);

        if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        }

        if (utilisateur.getActif() == null) {
            utilisateur.setActif(true);
        }
        if (utilisateur.getRole() == null) {
            utilisateur.setRole(Utilisateur.Role.USER);
        }

        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
        }
        utilisateurRepository.deleteById(id);
    }

    // ===============================
    // OPÉRATIONS AVEC DTO
    // ===============================

    /**
     * Créer un utilisateur depuis un DTO
     */
    @Transactional
    public Utilisateur saveFromDTO(UtilisateurDTO dto) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());

        if (dto.getActif() != null) {
            utilisateur.setActif(dto.getActif());
        }
        if (dto.getRole() != null) {
            utilisateur.setRole(dto.getRole());
        }

        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isEmpty()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        }

        // ✅ GESTION DES RÉGIMES ALIMENTAIRES
        if (dto.getRegimesIds() != null && !dto.getRegimesIds().isEmpty()) {
            Set<RegimeAlimentaire> regimes = new HashSet<>();
            for (Long regimeId : dto.getRegimesIds()) {
                RegimeAlimentaire regime = regimeAlimentaireRepository.findById(regimeId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Régime alimentaire non trouvé avec l'ID: " + regimeId
                        ));
                regimes.add(regime);
            }
            utilisateur.setRegimes(regimes);
        }

        // ✅ GESTION DES ALLERGÈNES
        if (dto.getAllergenesIds() != null && !dto.getAllergenesIds().isEmpty()) {
            Set<Allergene> allergenes = new HashSet<>();
            for (Long allergeneId : dto.getAllergenesIds()) {
                Allergene allergene = allergeneRepository.findById(allergeneId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Allergène non trouvé avec l'ID: " + allergeneId
                        ));
                allergenes.add(allergene);
            }
            utilisateur.setAllergenes(allergenes);
        }

        // ✅ GESTION DES TYPES DE CUISINE
        if (dto.getTypesCuisinePreferesIds() != null && !dto.getTypesCuisinePreferesIds().isEmpty()) {
            Set<TypeCuisine> typesCuisine = new HashSet<>();
            for (Long typeCuisineId : dto.getTypesCuisinePreferesIds()) {
                TypeCuisine typeCuisine = typeCuisineRepository.findById(typeCuisineId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Type de cuisine non trouvé avec l'ID: " + typeCuisineId
                        ));
                typesCuisine.add(typeCuisine);
            }
            utilisateur.setTypesCuisinePreferes(typesCuisine);
        }

        if (utilisateur.getActif() == null) {
            utilisateur.setActif(true);
        }
        if (utilisateur.getRole() == null) {
            utilisateur.setRole(Utilisateur.Role.USER);
        }

        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Mettre à jour un utilisateur depuis un DTO
     */
    @Transactional
    public Utilisateur updateFromDTO(Long id, UtilisateurDTO dto) {
        Utilisateur existing = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        existing.setEmail(dto.getEmail());
        existing.setNom(dto.getNom());
        existing.setPrenom(dto.getPrenom());

        if (dto.getActif() != null) {
            existing.setActif(dto.getActif());
        }
        if (dto.getRole() != null) {
            existing.setRole(dto.getRole());
        }

        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isEmpty()) {
            existing.setMotDePasse(dto.getMotDePasse());
        }

        // ✅ MISE À JOUR DES RÉGIMES ALIMENTAIRES
        if (dto.getRegimesIds() != null) {
            existing.getRegimes().clear();

            if (!dto.getRegimesIds().isEmpty()) {
                for (Long regimeId : dto.getRegimesIds()) {
                    if (regimeId != null && regimeId > 0) {
                        RegimeAlimentaire regime = regimeAlimentaireRepository.findById(regimeId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Régime alimentaire non trouvé avec l'ID: " + regimeId
                                ));
                        existing.getRegimes().add(regime);
                    }
                }
            }
        }

        // ✅ MISE À JOUR DES ALLERGÈNES
        if (dto.getAllergenesIds() != null) {
            existing.getAllergenes().clear();

            if (!dto.getAllergenesIds().isEmpty()) {
                for (Long allergeneId : dto.getAllergenesIds()) {
                    if (allergeneId != null && allergeneId > 0) {
                        Allergene allergene = allergeneRepository.findById(allergeneId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Allergène non trouvé avec l'ID: " + allergeneId
                                ));
                        existing.getAllergenes().add(allergene);
                    }
                }
            }
        }

        // ✅ MISE À JOUR DES TYPES DE CUISINE
        if (dto.getTypesCuisinePreferesIds() != null) {
            existing.getTypesCuisinePreferes().clear();

            if (!dto.getTypesCuisinePreferesIds().isEmpty()) {
                for (Long typeCuisineId : dto.getTypesCuisinePreferesIds()) {
                    if (typeCuisineId != null && typeCuisineId > 0) {
                        TypeCuisine typeCuisine = typeCuisineRepository.findById(typeCuisineId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Type de cuisine non trouvé avec l'ID: " + typeCuisineId
                                ));
                        existing.getTypesCuisinePreferes().add(typeCuisine);
                    }
                }
            }
        }

        return utilisateurRepository.save(existing);
    }

    // ===============================
    // GESTION DES TOKENS DE RÉINITIALISATION
    // ===============================

    @Transactional
    public String generatePasswordResetToken(Long utilisateurId) {
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUtilisateurId(utilisateurId);
        resetToken.setUsed(false);

        passwordResetTokenRepository.save(resetToken);
        return token;
    }

    public Optional<PasswordResetToken> findValidToken(String token) {
        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenRepository.findByToken(token);

        if (resetTokenOpt.isEmpty()) {
            return Optional.empty();
        }

        PasswordResetToken resetToken = resetTokenOpt.get();

        if (!resetToken.isValid()) {
            return Optional.empty();
        }

        return resetTokenOpt;
    }

    @Transactional
    public void markTokenAsUsed(String token) {
        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenRepository.findByToken(token);
        if (resetTokenOpt.isPresent()) {
            PasswordResetToken resetToken = resetTokenOpt.get();
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
        }
    }

    @Transactional
    public void updatePassword(Long utilisateurId, String hashedPassword) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + utilisateurId));

        utilisateur.setMotDePasse(hashedPassword);
        utilisateurRepository.save(utilisateur);
    }
}