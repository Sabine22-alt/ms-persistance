package com.mspersistance.univ.soa.controller;

import com.mspersistance.univ.soa.dto.FichierRecetteDTO;
import com.mspersistance.univ.soa.model.FichierRecette;
import com.mspersistance.univ.soa.service.FichierRecetteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/persistance/recettes/{recetteId}/fichiers")
@CrossOrigin(origins = "*")
@Tag(name = "Fichiers de Recettes", description = "Gestion des images et documents associés aux recettes")
public class FichierRecetteController {

    private final FichierRecetteService fichierRecetteService;

    @Autowired
    FichierRecetteController(FichierRecetteService fichierRecetteService) {
        this.fichierRecetteService = fichierRecetteService;
    }

    @PostMapping("/images")
    @Operation(summary = "Upload une image pour une recette")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long recetteId,
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le fichier est vide"));
            }

            FichierRecetteDTO fichierDTO = fichierRecetteService.uploadImage(recetteId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(fichierDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Erreur lors de l'upload: " + e.getMessage()));
        }
    }

    @PostMapping("/documents")
    @Operation(summary = "Upload un document pour une recette")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long recetteId,
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Le fichier est vide"));
            }

            FichierRecetteDTO fichierDTO = fichierRecetteService.uploadDocument(recetteId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(fichierDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Erreur lors de l'upload: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les fichiers d'une recette")
    public ResponseEntity<List<FichierRecetteDTO>> getAllFichiers(@PathVariable Long recetteId) {
        List<FichierRecetteDTO> fichiers = fichierRecetteService.getFichiersByRecette(recetteId);
        return ResponseEntity.ok(fichiers);
    }

    @GetMapping("/images")
    @Operation(summary = "Récupérer les images d'une recette")
    public ResponseEntity<List<FichierRecetteDTO>> getImages(@PathVariable Long recetteId) {
        List<FichierRecetteDTO> images = fichierRecetteService.getImagesByRecette(recetteId);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/documents")
    @Operation(summary = "Récupérer les documents d'une recette")
    public ResponseEntity<List<FichierRecetteDTO>> getDocuments(@PathVariable Long recetteId) {
        List<FichierRecetteDTO> documents = fichierRecetteService.getDocumentsByRecette(recetteId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{fichierId}/download")
    @Operation(summary = "Télécharger un fichier")
    public ResponseEntity<?> downloadFichier(@PathVariable Long recetteId, @PathVariable Long fichierId) {
        try {
            FichierRecetteDTO fichier = fichierRecetteService.getFichierById(fichierId);
            InputStream inputStream = fichierRecetteService.downloadFichier(fichierId);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fichier.getNomOriginal() + "\"");
            headers.setContentType(MediaType.parseMediaType(fichier.getContentType()));

            return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Erreur lors du téléchargement: " + e.getMessage()));
        }
    }

    @GetMapping("/{fichierId}")
    @Operation(summary = "Récupérer les métadonnées d'un fichier")
    public ResponseEntity<?> getFichierMetadata(@PathVariable Long recetteId, @PathVariable Long fichierId) {
        try {
            FichierRecetteDTO fichier = fichierRecetteService.getFichierById(fichierId);
            return ResponseEntity.ok(fichier);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Fichier non trouvé"));
        }
    }

    @DeleteMapping("/{fichierId}")
    @Operation(summary = "Supprimer un fichier")
    public ResponseEntity<?> deleteFichier(@PathVariable Long recetteId, @PathVariable Long fichierId) {
        try {
            fichierRecetteService.deleteFichier(fichierId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    @DeleteMapping
    @Operation(summary = "Supprimer tous les fichiers d'une recette")
    public ResponseEntity<?> deleteAllFichiers(@PathVariable Long recetteId) {
        try {
            fichierRecetteService.deleteAllFichiersByRecette(recetteId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    @GetMapping("/images/{fichierId}/content")
    @Operation(summary = "Servir une image en streaming (inline)")
    public ResponseEntity<?> streamImage(
            @PathVariable Long recetteId,
            @PathVariable Long fichierId) {

        try {
            FichierRecetteDTO fichier = fichierRecetteService.getFichierById(fichierId);

            if (!fichier.getRecetteId().equals(recetteId) || fichier.getType() != FichierRecette.TypeFichier.IMAGE) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Image non trouvée pour cette recette"));
            }

            InputStream inputStream = fichierRecetteService.downloadFichier(fichierId);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fichier.getNomOriginal() + "\"");
            headers.setContentType(MediaType.parseMediaType(fichier.getContentType()));

            return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Erreur lors de la récupération de l'image: " + e.getMessage()));
        }
    }

    @GetMapping("/{fichierId}/content")
    @Operation(summary = "Servir un fichier en streaming (inline)")
    public ResponseEntity<?> streamAny(
            @PathVariable Long recetteId,
            @PathVariable Long fichierId) {

        try {
            FichierRecetteDTO fichier = fichierRecetteService.getFichierById(fichierId);

            if (!fichier.getRecetteId().equals(recetteId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Fichier non trouvé pour cette recette"));
            }

            InputStream inputStream = fichierRecetteService.downloadFichier(fichierId);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fichier.getNomOriginal() + "\"");
            headers.setContentType(MediaType.parseMediaType(fichier.getContentType()));

            return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Erreur lors de la récupération du fichier: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
