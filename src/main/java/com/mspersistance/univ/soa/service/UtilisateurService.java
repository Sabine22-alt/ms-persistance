package com.mspersistance.univ.soa.service;

import com.mspersistance.univ.soa.dto.UtilisateurDTO;
import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.factory.UtilisateurFactory;
import com.mspersistance.univ.soa.model.Aliment;
import com.mspersistance.univ.soa.model.PasswordResetToken;
import com.mspersistance.univ.soa.model.Utilisateur;
import com.mspersistance.univ.soa.repository.AlimentRepository;
import com.mspersistance.univ.soa.repository.PasswordResetTokenRepository;
import com.mspersistance.univ.soa.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour la gestion des utilisateurs.
 */
@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final AlimentRepository alimentRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UtilisateurFactory utilisateurFactory;

    public UtilisateurService(
            UtilisateurRepository utilisateurRepository,
            AlimentRepository alimentRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetTokenRepository passwordResetTokenRepository,
            UtilisateurFactory utilisateurFactory) {
        this.utilisateurRepository = utilisateurRepository;
        this.alimentRepository = alimentRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.utilisateurFactory = utilisateurFactory;
    }

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
    public Utilisateur update(Long id, Utilisateur utilisateur) {
        Utilisateur existing = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        existing.setEmail(utilisateur.getEmail());
        existing.setNom(utilisateur.getNom());
        existing.setPrenom(utilisateur.getPrenom());
        existing.setTelephone(utilisateur.getTelephone());
        existing.setBio(utilisateur.getBio());
        existing.setAdresse(utilisateur.getAdresse());

        if (utilisateur.getActif() != null) {
            existing.setActif(utilisateur.getActif());
        }
        if (utilisateur.getRole() != null) {
            existing.setRole(utilisateur.getRole());
        }
        if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty()) {
            existing.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        }
        if (utilisateur.getAlimentsExclus() != null) {
            existing.setAlimentsExclus(utilisateur.getAlimentsExclus());
        }

        return utilisateurRepository.save(existing);
    }

    @Transactional
    public Utilisateur saveFromDTO(UtilisateurDTO dto) {
        return utilisateurRepository.save(
                utilisateurFactory.createFromDTO(dto)
        );
    }

    @Transactional
    public Utilisateur updateFromDTO(Long id, UtilisateurDTO dto) {
        Utilisateur existing = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        return utilisateurRepository.save(
                utilisateurFactory.updateFromDTO(existing, dto)
        );
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

    @Transactional
    public boolean resetPasswordWithToken(String token, String newPassword) {
        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenRepository.findByToken(token);

        if (resetTokenOpt.isEmpty() || !resetTokenOpt.get().isValid()) {
            return false;
        }

        PasswordResetToken resetToken = resetTokenOpt.get();
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(resetToken.getUtilisateurId());

        if (utilisateurOpt.isEmpty()) {
            return false;
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        utilisateur.setMotDePasse(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(utilisateur);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return true;
    }
}
