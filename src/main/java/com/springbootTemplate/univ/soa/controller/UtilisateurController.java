package com.springbootTemplate.univ.soa.controller;

import com.springbootTemplate.univ.soa.dto.UtilisateurDTO;
import com.springbootTemplate.univ.soa.mapper.UtilisateurMapper;
import com.springbootTemplate.univ.soa.model.Utilisateur;
import com.springbootTemplate.univ.soa.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/persistance/utilisateurs")
@CrossOrigin(origins = "*")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private UtilisateurMapper utilisateurMapper;

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
     * POST /api/persistance/utilisateurs - Créer un nouvel utilisateur
     */
    @PostMapping
    public ResponseEntity<?> createUtilisateur(@RequestBody UtilisateurDTO dto) {
        // Validation : email requis
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'email est obligatoire"));
        }

        // Validation : format email
        if (!isValidEmail(dto.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Format d'email invalide"));
        }

        // Validation : email unique
        if (utilisateurService.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Un utilisateur avec cet email existe déjà"));
        }

        // Validation : mot de passe requis
        if (dto.getMotDePasse() == null || dto.getMotDePasse().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le mot de passe est obligatoire"));
        }

        // Validation : longueur mot de passe
        if (dto.getMotDePasse().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le mot de passe doit contenir au moins 6 caractères"));
        }

        // Validation : nom et prénom requis
        if (dto.getNom() == null || dto.getNom().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le nom est obligatoire"));
        }

        if (dto.getPrenom() == null || dto.getPrenom().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le prénom est obligatoire"));
        }

        // Validation : role valide
        if (dto.getRole() != null &&
                dto.getRole() != Utilisateur.Role.USER &&
                dto.getRole() != Utilisateur.Role.ADMIN) {
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
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'email est obligatoire"));
        }

        // Validation : format email
        if (!isValidEmail(dto.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Format d'email invalide"));
        }

        // Validation : email unique (sauf pour l'utilisateur actuel)
        utilisateurService.findByEmail(dto.getEmail()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(id)) {
                throw new IllegalStateException("Un autre utilisateur utilise déjà cet email");
            }
        });

        // Validation : si mot de passe fourni, vérifier la longueur
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().trim().isEmpty()) {
            if (dto.getMotDePasse().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Le mot de passe doit contenir au moins 6 caractères"));
            }
        }

        // Validation : nom et prénom requis
        if (dto.getNom() == null || dto.getNom().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le nom est obligatoire"));
        }

        if (dto.getPrenom() == null || dto.getPrenom().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le prénom est obligatoire"));
        }

        try {
            Utilisateur updated = utilisateurService.updateFromDTO(id, dto);
            UtilisateurDTO responseDto = utilisateurMapper.toDTO(updated);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(e.getMessage()));
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

    // Méthodes utilitaires
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}