package com.springbootTemplate.univ.soa.controller;

import com.springbootTemplate.univ.soa.dto.ActiviteDTO;
import com.springbootTemplate.univ.soa.mapper.ActiviteMapper;
import com.springbootTemplate.univ.soa.service.ActiviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/persistance/utilisateurs")
@CrossOrigin(origins = "*")
public class ActiviteController {

    @Autowired
    private ActiviteService activiteService;

    @Autowired
    private ActiviteMapper activiteMapper;

    /**
     * GET /api/persistance/utilisateurs/{utilisateurId}/activite
     * Récupère l'historique complet d'activité d'un utilisateur
     */
    @GetMapping("/{utilisateurId}/activite")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ActiviteDTO>> getActivitesByUtilisateur(@PathVariable Long utilisateurId) {
        List<ActiviteDTO> dtos = activiteService.getActivitesByUtilisateur(utilisateurId)
            .stream()
            .map(activiteMapper::toDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/persistance/utilisateurs/{utilisateurId}/activite/recent
     * Récupère les 10 dernières activités d'un utilisateur (pour le dashboard)
     */
    @GetMapping("/{utilisateurId}/activite/recent")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ActiviteDTO>> getRecentActivites(@PathVariable Long utilisateurId) {
        List<ActiviteDTO> dtos = activiteService.getLast10Activites(utilisateurId)
            .stream()
            .map(activiteMapper::toDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}

