package com.mspersistance.univ.soa.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mspersistance.univ.soa.dto.UtilisateurDTO;
import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.factory.UtilisateurFactory;
import com.mspersistance.univ.soa.model.Aliment;
import com.mspersistance.univ.soa.model.Allergene;
import com.mspersistance.univ.soa.model.PasswordResetToken;
import com.mspersistance.univ.soa.model.RegimeAlimentaire;
import com.mspersistance.univ.soa.model.TypeCuisine;
import com.mspersistance.univ.soa.model.Utilisateur;
import com.mspersistance.univ.soa.repository.AlimentRepository;
import com.mspersistance.univ.soa.repository.AllergeneRepository;
import com.mspersistance.univ.soa.repository.PasswordResetTokenRepository;
import com.mspersistance.univ.soa.repository.RegimeAlimentaireRepository;
import com.mspersistance.univ.soa.repository.TypeCuisineRepository;
import com.mspersistance.univ.soa.repository.UtilisateurRepository;

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
    private final RegimeAlimentaireRepository regimeAlimentaireRepository;
    private final AllergeneRepository allergeneRepository;
    private final TypeCuisineRepository typeCuisineRepository;

    public UtilisateurService(
            UtilisateurRepository utilisateurRepository,
            AlimentRepository alimentRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetTokenRepository passwordResetTokenRepository,
            UtilisateurFactory utilisateurFactory,
            RegimeAlimentaireRepository regimeAlimentaireRepository,
            AllergeneRepository allergeneRepository,
            TypeCuisineRepository typeCuisineRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.alimentRepository = alimentRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.utilisateurFactory = utilisateurFactory;
        this.regimeAlimentaireRepository = regimeAlimentaireRepository;
        this.allergeneRepository = allergeneRepository;
        this.typeCuisineRepository = typeCuisineRepository;
    }

    // ===============================
    // OPÃ‰RATIONS CRUD DE BASE
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
    public void delete(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
        }
        utilisateurRepository.deleteById(id);
    }

    @Transactional
    public Utilisateur saveFromDTO(UtilisateurDTO dto) {
        // Validation des régimes alimentaires
        if (dto.regimesIds() != null && !dto.regimesIds().isEmpty()) {
            for (Long regimeId : dto.regimesIds()) {
                if (!regimeAlimentaireRepository.existsById(regimeId)) {
                    throw new ResourceNotFoundException("Régime alimentaire non trouvé avec l'ID: " + regimeId);
                }
            }
        }

        // Validation des allergènes
        if (dto.allergenesIds() != null && !dto.allergenesIds().isEmpty()) {
            for (Long allergeneId : dto.allergenesIds()) {
                if (!allergeneRepository.existsById(allergeneId)) {
                    throw new ResourceNotFoundException("Allergène non trouvé avec l'ID: " + allergeneId);
                }
            }
        }

        // Validation des types de cuisine
        if (dto.typesCuisinePreferesIds() != null && !dto.typesCuisinePreferesIds().isEmpty()) {
            for (Long typeCuisineId : dto.typesCuisinePreferesIds()) {
                if (!typeCuisineRepository.existsById(typeCuisineId)) {
                    throw new ResourceNotFoundException("Type de cuisine non trouvé avec l'ID: " + typeCuisineId);
                }
            }
        }

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

    // ===============================
    // GESTION DES TOKENS DE RÃ‰INITIALISATION
    // ===============================

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

    // ===============================
    // GESTION DES PRÉFÉRENCES ALIMENTAIRES
    // ===============================

    @Transactional
    public void addRegime(Long userId, Long regimeId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        RegimeAlimentaire regime = regimeAlimentaireRepository.findById(regimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Régime non trouvé"));

        utilisateur.getRegimesAlimentaires().add(regime);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void removeRegime(Long userId, Long regimeId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        utilisateur.getRegimesAlimentaires().removeIf(r -> r.getId().equals(regimeId));
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void addAllergene(Long userId, Long allergeneId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        Allergene allergene = allergeneRepository.findById(allergeneId)
                .orElseThrow(() -> new ResourceNotFoundException("Allergène non trouvé"));

        utilisateur.getAllergenes().add(allergene);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void removeAllergene(Long userId, Long allergeneId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        utilisateur.getAllergenes().removeIf(a -> a.getId().equals(allergeneId));
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void addTypeCuisine(Long userId, Long typeCuisineId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        TypeCuisine typeCuisine = typeCuisineRepository.findById(typeCuisineId)
                .orElseThrow(() -> new ResourceNotFoundException("Type de cuisine non trouvé"));

        utilisateur.getTypesCuisinePreferences().add(typeCuisine);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void removeTypeCuisine(Long userId, Long typeCuisineId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        utilisateur.getTypesCuisinePreferences().removeIf(t -> t.getId().equals(typeCuisineId));
        utilisateurRepository.save(utilisateur);
    }

    public Set<RegimeAlimentaire> getRegimes(Long userId) {
        return utilisateurRepository.findById(userId)
                .map(Utilisateur::getRegimesAlimentaires)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    public Set<Allergene> getAllergenes(Long userId) {
        return utilisateurRepository.findById(userId)
                .map(Utilisateur::getAllergenes)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    public Set<TypeCuisine> getTypesCuisinePreferences(Long userId) {
        return utilisateurRepository.findById(userId)
                .map(Utilisateur::getTypesCuisinePreferences)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
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

    public Optional<PasswordResetToken> findValidToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .filter(PasswordResetToken::isValid);
    }

    @Transactional
    public void updatePassword(Long userId, String hashedPassword) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));
        utilisateur.setMotDePasse(hashedPassword);
        utilisateurRepository.save(utilisateur);
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
}
