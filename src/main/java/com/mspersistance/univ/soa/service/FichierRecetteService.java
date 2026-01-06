package com.mspersistance.univ.soa.service;

import com.mspersistance.univ.soa.dto.FichierRecetteDTO;
import com.mspersistance.univ.soa.exception.ResourceNotFoundException;
import com.mspersistance.univ.soa.model.FichierRecette;
import com.mspersistance.univ.soa.model.Recette;
import com.mspersistance.univ.soa.repository.FichierRecetteRepository;
import com.mspersistance.univ.soa.repository.RecetteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FichierRecetteService {

    @Autowired
    private FichierRecetteRepository fichierRecetteRepository;

    @Autowired
    private RecetteRepository recetteRepository;

    @Autowired
    private MinioService minioService;

    @Value("${server.public-url}")
    private String serverPublicUrl;

    private static final List<String> IMAGE_TYPES = List.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> DOCUMENT_TYPES = List.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain"
    );

    @Transactional
    @CacheEvict(value = {"recettes", "fichiers"}, allEntries = true)
    public FichierRecetteDTO uploadImage(Long recetteId, MultipartFile file) {
        Recette recette = recetteRepository.findById(recetteId)
            .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + recetteId));

        if (!IMAGE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Type de fichier non autorisé. Types acceptés: JPEG, PNG, GIF, WEBP");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("La taille du fichier ne peut pas dépasser 10MB");
        }

        String uniqueFileName = minioService.generateUniqueFileName(file.getOriginalFilename());
        String objectPath = "recettes/" + recetteId + "/images/" + uniqueFileName;

        minioService.uploadFile(file, minioService.getRecettesBucket(), objectPath);

        FichierRecette fichier = new FichierRecette();
        fichier.setNomOriginal(file.getOriginalFilename());
        fichier.setNomStocke(uniqueFileName);
        fichier.setContentType(file.getContentType());
        fichier.setTaille(file.getSize());
        fichier.setType(FichierRecette.TypeFichier.IMAGE);
        fichier.setCheminMinio(objectPath);
        fichier.setRecette(recette);

        fichier = fichierRecetteRepository.save(fichier);

        // URL publique absolue (backend streaming) accessible depuis n'importe où
        String streamUrl = String.format("%s/api/persistance/recettes/%d/fichiers/images/%d/content",
            serverPublicUrl, recetteId, fichier.getId());

        recette.setImageUrl(streamUrl);
        recetteRepository.save(recette);

        System.out.println("✅ Image uploadée; URL publique absolue: " + streamUrl);

        return toDTO(fichier);
    }

    @Transactional
    @CacheEvict(value = {"recettes", "fichiers"}, allEntries = true)
    public FichierRecetteDTO uploadDocument(Long recetteId, MultipartFile file) {
        Recette recette = recetteRepository.findById(recetteId)
            .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + recetteId));

        if (!DOCUMENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Type de fichier non autorisé. Types acceptés: PDF, DOC, DOCX, TXT");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("La taille du fichier ne peut pas dépasser 10MB");
        }

        String uniqueFileName = minioService.generateUniqueFileName(file.getOriginalFilename());
        String objectPath = "recettes/" + recetteId + "/documents/" + uniqueFileName;

        minioService.uploadFile(file, minioService.getDocumentsBucket(), objectPath);

        FichierRecette fichier = new FichierRecette();
        fichier.setNomOriginal(file.getOriginalFilename());
        fichier.setNomStocke(uniqueFileName);
        fichier.setContentType(file.getContentType());
        fichier.setTaille(file.getSize());
        fichier.setType(FichierRecette.TypeFichier.DOCUMENT);
        fichier.setCheminMinio(objectPath);
        fichier.setRecette(recette);

        fichier = fichierRecetteRepository.save(fichier);

        return toDTO(fichier);
    }

    @Cacheable(value = "fichiers", key = "'recette:' + #recetteId")
    public List<FichierRecetteDTO> getFichiersByRecette(Long recetteId) {
        return fichierRecetteRepository.findByRecetteId(recetteId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "fichiers", key = "'recette:' + #recetteId + ':images'")
    public List<FichierRecetteDTO> getImagesByRecette(Long recetteId) {
        return fichierRecetteRepository.findByRecetteIdAndType(recetteId, FichierRecette.TypeFichier.IMAGE).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "fichiers", key = "'recette:' + #recetteId + ':documents'")
    public List<FichierRecetteDTO> getDocumentsByRecette(Long recetteId) {
        return fichierRecetteRepository.findByRecetteIdAndType(recetteId, FichierRecette.TypeFichier.DOCUMENT).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public InputStream downloadFichier(Long fichierId) {
        FichierRecette fichier = fichierRecetteRepository.findById(fichierId)
            .orElseThrow(() -> new ResourceNotFoundException("Fichier non trouvé avec l'ID: " + fichierId));

        String bucketName = fichier.getType() == FichierRecette.TypeFichier.IMAGE
            ? minioService.getRecettesBucket()
            : minioService.getDocumentsBucket();

        return minioService.downloadFile(bucketName, fichier.getCheminMinio());
    }

    @Cacheable(value = "fichiers", key = "#fichierId")
    public FichierRecetteDTO getFichierById(Long fichierId) {
        FichierRecette fichier = fichierRecetteRepository.findById(fichierId)
            .orElseThrow(() -> new ResourceNotFoundException("Fichier non trouvé avec l'ID: " + fichierId));
        return toDTO(fichier);
    }

    @Transactional
    @CacheEvict(value = {"recettes", "fichiers"}, allEntries = true)
    public void deleteFichier(Long fichierId) {
        FichierRecette fichier = fichierRecetteRepository.findById(fichierId)
            .orElseThrow(() -> new ResourceNotFoundException("Fichier non trouvé avec l'ID: " + fichierId));

        String bucketName = fichier.getType() == FichierRecette.TypeFichier.IMAGE
            ? minioService.getRecettesBucket()
            : minioService.getDocumentsBucket();

        minioService.deleteFile(bucketName, fichier.getCheminMinio());

        fichierRecetteRepository.delete(fichier);
    }

    @Transactional
    @CacheEvict(value = {"recettes", "fichiers"}, allEntries = true)
    public void deleteAllFichiersByRecette(Long recetteId) {
        List<FichierRecette> fichiers = fichierRecetteRepository.findByRecetteId(recetteId);

        for (FichierRecette fichier : fichiers) {
            String bucketName = fichier.getType() == FichierRecette.TypeFichier.IMAGE
                ? minioService.getRecettesBucket()
                : minioService.getDocumentsBucket();

            minioService.deleteFile(bucketName, fichier.getCheminMinio());
        }

        fichierRecetteRepository.deleteByRecetteId(recetteId);
    }

    private FichierRecetteDTO toDTO(FichierRecette fichier) {
        FichierRecetteDTO dto = new FichierRecetteDTO();
        dto.setId(fichier.getId());
        dto.setNomOriginal(fichier.getNomOriginal());
        dto.setNomStocke(fichier.getNomStocke());
        dto.setContentType(fichier.getContentType());
        dto.setTaille(fichier.getTaille());
        dto.setType(fichier.getType());
        dto.setRecetteId(fichier.getRecette().getId());
        dto.setDateUpload(fichier.getDateUpload());

        String bucketName = fichier.getType() == FichierRecette.TypeFichier.IMAGE
            ? minioService.getRecettesBucket()
            : minioService.getDocumentsBucket();
        dto.setUrlTelechargement(minioService.getPresignedUrl(bucketName, fichier.getCheminMinio()));
        dto.setUrlStream(String.format("/api/persistance/recettes/%d/fichiers/images/%d/content", fichier.getRecette().getId(), fichier.getId()));

        return dto;
    }
}
