package com.mspersistance.univ.soa.controller;

import com.springbootTemplate.univ.soa.dto.UtilisateurDTO;
import com.springbootTemplate.univ.soa.mapper.UtilisateurMapper;
import com.springbootTemplate.univ.soa.model.PasswordResetToken;
import com.springbootTemplate.univ.soa.model.Utilisateur;
import com.springbootTemplate.univ.soa.service.UtilisateurService;
import com.mspersistance.univ.soa.dto.UtilisateurDTO;
import com.mspersistance.univ.soa.mapper.UtilisateurMapper;
import com.mspersistance.univ.soa.model.Utilisateur;
import com.mspersistance.univ.soa.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/persistance/utilisateurs")
@CrossOrigin(origins = "*")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final UtilisateurMapper utilisateurMapper;

    @Autowired
    UtilisateurController(UtilisateurService utilisateurService,
                          UtilisateurMapper utilisateurMapper) {
        this.utilisateurService = utilisateurService;
        this.utilisateurMapper = utilisateurMapper;
    }

    /**
     * GET /api/persistance/utilisateurs - Récupérer tous les utilisateurs
     */
    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> getAllUtilisateurs() {
        List<UtilisateurDTO> dtos = utilisateurService.findAll().stream()
                .map(utilisateurMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/utilisateurs/{id} - Récupérer un utilisateur par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> getUtilisateurById(@PathVariable Long id) {
        return utilisateurService.findById(id)
                .map(utilisateurMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/persistance/utilisateurs/email/{email} - Récupérer un utilisateur par email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurDTO> getUtilisateurByEmail(@PathVariable String email) {
        return utilisateurService.findByEmail(email)
                .map(utilisateurMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/persistance/utilisateurs - Créer un nouvel utilisateur
     */
    @PostMapping
    public ResponseEntity<?> createUtilisateur(@RequestBody UtilisateurDTO dto) {
        // Validation : email requis
        if (dto.email() == null || dto.email().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'email est obligatoire"));
        }

        // Validation : format email
        if (!isValidEmail(dto.email())) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Format d'email invalide"));
        }

        // Validation : email unique
        if (utilisateurService.findByEmail(dto.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Un utilisateur avec cet email existe déjà"));
        }

        // Validation : mot de passe requis
        if (dto.motDePasse() == null || dto.motDePasse().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le mot de passe est obligatoire"));
        }

        // Validation : longueur mot de passe
        if (dto.motDePasse().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le mot de passe doit contenir au moins 6 caractères"));
        }

        // Validation : nom et prénom requis
        if (dto.nom() == null || dto.nom().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le nom est obligatoire"));
        }

        if (dto.prenom() == null || dto.prenom().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le prénom est obligatoire"));
        }

        // Validation : role valide
        if (dto.role() != null &&
                dto.role() != Utilisateur.Role.USER &&
                dto.role() != Utilisateur.Role.ADMIN) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le rôle doit être USER ou ADMIN"));
        }

        try {
            Utilisateur saved = utilisateurService.saveFromDTO(dto);
            UtilisateurDTO responseDto = utilisateurMapper.toDTO(saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la création: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/persistance/utilisateurs/{id} - Mettre à jour un utilisateur
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUtilisateur(
            @PathVariable Long id,
            @RequestBody UtilisateurDTO dto) {

        // Vérifier que l'utilisateur existe
        if (utilisateurService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Validation : email requis
        if (dto.email() == null || dto.email().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'email est obligatoire"));
        }

        // Validation : format email
        if (!isValidEmail(dto.email())) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Format d'email invalide"));
        }

        // Validation : email unique (sauf pour l'utilisateur actuel)
        java.util.Optional<Utilisateur> existingOpt = utilisateurService.findByEmail(dto.email());
        if (existingOpt.isPresent() && !existingOpt.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Un autre utilisateur utilise déjà cet email"));
        }

        // Validation : si mot de passe fourni, vérifier la longueur
        if (dto.motDePasse() != null && !dto.motDePasse().trim().isEmpty()) {
            if (dto.motDePasse().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Le mot de passe doit contenir au moins 6 caractères"));
            }
        }

        // Validation : nom et prénom requis
        if (dto.nom() == null || dto.nom().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le nom est obligatoire"));
        }

        if (dto.prenom() == null || dto.prenom().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le prénom est obligatoire"));
        }

        try {
            Utilisateur updated = utilisateurService.updateFromDTO(id, dto);
            UtilisateurDTO responseDto = utilisateurMapper.toDTO(updated);
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/persistance/utilisateurs/{id} - Supprimer un utilisateur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUtilisateur(@PathVariable Long id) {
        // Vérifier que l'utilisateur existe
        if (utilisateurService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Utilisateur non trouvé avec l'ID: " + id));
        }

        try {
            utilisateurService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    /**
     * GET /api/persistance/utilisateurs/auth/{email} - Récupérer les infos d'authentification
     * Cet endpoint retourne le hash du mot de passe pour permettre la validation côté ms-utilisateur
     */
    @GetMapping("/auth/{email}")
    public ResponseEntity<UtilisateurDTO> getUtilisateurForAuth(@PathVariable String email) {
        Optional<Utilisateur> utilisateurOpt = utilisateurService.findByEmail(email);

        if (utilisateurOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        // Créer un DTO avec le hash du mot de passe (uniquement pour l'auth)
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(utilisateur.getId());
        dto.setEmail(utilisateur.getEmail());
        dto.setMotDePasse(utilisateur.getMotDePasse()); // ✅ Hash inclus
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setActif(utilisateur.getActif());
        dto.setRole(utilisateur.getRole());

        return ResponseEntity.ok(dto);
    }

    /**
     * POST /api/persistance/utilisateurs/{id}/generate-reset-token
     */
    @PostMapping("/{id}/generate-reset-token")
    public ResponseEntity<Map<String, String>> generateResetToken(@PathVariable Long id) {
        String token = utilisateurService.generatePasswordResetToken(id);
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * GET /api/persistance/utilisateurs/validate-token/{token}
     */
    @GetMapping("/validate-token/{token}")
    public ResponseEntity<?> validateToken(@PathVariable String token) {
        Optional<PasswordResetToken> resetTokenOpt = utilisateurService.findValidToken(token);

        if (resetTokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Token invalide ou expiré"));
        }

        PasswordResetToken resetToken = resetTokenOpt.get();
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "utilisateurId", resetToken.getUtilisateurId()
        ));
    }

    /**
     * PUT /api/persistance/utilisateurs/{id}/update-password
     */
    @PutMapping("/{id}/update-password")
    public ResponseEntity<?> updatePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String hashedPassword = request.get("hashedPassword");

        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le mot de passe hashé est requis"));
        }

        utilisateurService.updatePassword(id, hashedPassword);
        return ResponseEntity.ok(Map.of("message", "Mot de passe mis à jour"));
    }

    /**
     * POST /api/persistance/utilisateurs/mark-token-used/{token}
     */
    @PostMapping("/mark-token-used/{token}")
    public ResponseEntity<?> markTokenAsUsed(@PathVariable String token) {
        utilisateurService.markTokenAsUsed(token);
        return ResponseEntity.ok(Map.of("message", "Token marqué comme utilisé"));
    }
}