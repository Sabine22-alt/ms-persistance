package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.dto.UtilisateurDTO;
import com.springbootTemplate.univ.soa.exception.ResourceNotFoundException;
import com.springbootTemplate.univ.soa.model.Aliment;
import com.springbootTemplate.univ.soa.model.PasswordResetToken;
import com.springbootTemplate.univ.soa.model.Utilisateur;
import com.springbootTemplate.univ.soa.repository.AlimentRepository;
import com.springbootTemplate.univ.soa.repository.PasswordResetTokenRepository;
import com.springbootTemplate.univ.soa.repository.UtilisateurRepository;
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
    private AlimentRepository alimentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

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

        // Hasher le mot de passe
        if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        }

        // Ensure defaults if null
        if (utilisateur.getActif() == null) {
            utilisateur.setActif(true);
        }
        if (utilisateur.getRole() == null) {
            utilisateur.setRole(Utilisateur.Role.USER);
        }

        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public Utilisateur update(Long id, Utilisateur utilisateur) {
        Utilisateur existing = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        existing.setEmail(utilisateur.getEmail());
        existing.setNom(utilisateur.getNom());
        existing.setPrenom(utilisateur.getPrenom());
        // Only update actif/role if provided (avoid setting null into NOT NULL column)
        if (utilisateur.getActif() != null) {
            existing.setActif(utilisateur.getActif());
        }
        if (utilisateur.getRole() != null) {
            existing.setRole(utilisateur.getRole());
        }

        // Mettre à jour le mot de passe seulement s'il est fourni
        if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty()) {
            existing.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        }

        // Mettre à jour les aliments exclus
        if (utilisateur.getAlimentsExclus() != null) {
            existing.setAlimentsExclus(utilisateur.getAlimentsExclus());
        }

        return utilisateurRepository.save(existing);
    }

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

        if (dto.getAlimentsExclusIds() != null && !dto.getAlimentsExclusIds().isEmpty()) {
            Set<Aliment> alimentsExclus = new HashSet<>();
            for (Long alimentId : dto.getAlimentsExclusIds()) {
                Aliment aliment = alimentRepository.findById(alimentId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Aliment non trouvé avec l'ID: " + alimentId
                        ));
                alimentsExclus.add(aliment);
            }
            utilisateur.setAlimentsExclus(alimentsExclus);
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
        // Only update actif/role if provided
        if (dto.getActif() != null) {
            existing.setActif(dto.getActif());
        }
        if (dto.getRole() != null) {
            existing.setRole(dto.getRole());
        }

        // Mettre à jour le mot de passe seulement s'il est fourni
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isEmpty()) {
            existing.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        }

        // Mettre à jour les aliments exclus
        existing.getAlimentsExclus().clear();

        if (dto.getAlimentsExclusIds() != null && !dto.getAlimentsExclusIds().isEmpty()) {
            Set<Aliment> alimentsExclus = new HashSet<>();
            for (Long alimentId : dto.getAlimentsExclusIds()) {
                Aliment aliment = alimentRepository.findById(alimentId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Aliment non trouvé avec l'ID: " + alimentId
                        ));
                alimentsExclus.add(aliment);
            }
            existing.setAlimentsExclus(alimentsExclus);
        }

        return utilisateurRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
        }
        utilisateurRepository.deleteById(id);
    }

    @Transactional
    public void addAlimentExclu(Long userId, Long alimentId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Aliment aliment = alimentRepository.findById(alimentId)
                .orElseThrow(() -> new ResourceNotFoundException("Aliment non trouvé"));

        utilisateur.getAlimentsExclus().add(aliment);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void removeAlimentExclu(Long userId, Long alimentId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        utilisateur.getAlimentsExclus().removeIf(a -> a.getId().equals(alimentId));
        utilisateurRepository.save(utilisateur);
    }

    /**
     * Générer un token de réinitialisation de mot de passe
     */
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

    /**
     * Valider et réinitialiser le mot de passe avec un token
     */
    @Transactional
    public boolean resetPasswordWithToken(String token, String newPassword) {
        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenRepository.findByToken(token);

        if (resetTokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = resetTokenOpt.get();

        // Vérifier que le token est valide (non utilisé et non expiré)
        if (!resetToken.isValid()) {
            return false;
        }

        // Récupérer l'utilisateur
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(resetToken.getUtilisateurId());
        if (utilisateurOpt.isEmpty()) {
            return false;
        }

        // Mettre à jour le mot de passe
        Utilisateur utilisateur = utilisateurOpt.get();
        utilisateur.setMotDePasse(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(utilisateur);

        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return true;
    }
}