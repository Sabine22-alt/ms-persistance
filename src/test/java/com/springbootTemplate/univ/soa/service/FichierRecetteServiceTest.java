package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.dto.FichierRecetteDTO;
import com.springbootTemplate.univ.soa.exception.ResourceNotFoundException;
import com.springbootTemplate.univ.soa.model.FichierRecette;
import com.springbootTemplate.univ.soa.model.Recette;
import com.springbootTemplate.univ.soa.repository.FichierRecetteRepository;
import com.springbootTemplate.univ.soa.repository.RecetteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests unitaires pour FichierRecetteService")
class FichierRecetteServiceTest {

    @Mock
    private FichierRecetteRepository fichierRecetteRepository;

    @Mock
    private RecetteRepository recetteRepository;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private FichierRecetteService fichierRecetteService;

    private Recette recette;
    private FichierRecette fichierRecette;

    @BeforeEach
    void setUp() {
        recette = new Recette();
        recette.setId(1L);
        recette.setTitre("Test Recette");

        fichierRecette = new FichierRecette();
        fichierRecette.setId(1L);
        fichierRecette.setNomOriginal("test.jpg");
        fichierRecette.setNomStocke("unique-test.jpg");
        fichierRecette.setContentType("image/jpeg");
        fichierRecette.setTaille(1024L);
        fichierRecette.setType(FichierRecette.TypeFichier.IMAGE);
        fichierRecette.setCheminMinio("recettes/1/images/unique-test.jpg");
        fichierRecette.setRecette(recette);
        fichierRecette.setDateUpload(LocalDateTime.now());
    }

    @BeforeEach
    void setUpSecurity() {
        SecurityContextHolder.setContext(mock(org.springframework.security.core.context.SecurityContext.class));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-user");
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
    }

    @Test
    @DisplayName("Devrait uploader une image avec succès")
    void testUploadImage_Success() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024L);

        when(recetteRepository.findById(1L)).thenReturn(Optional.of(recette));
        when(minioService.generateUniqueFileName(anyString())).thenReturn("unique-test.jpg");
        when(minioService.uploadFile(any(), anyString(), anyString())).thenReturn("unique-test.jpg");
        when(minioService.getRecettesBucket()).thenReturn("recettes-bucket");
        when(fichierRecetteRepository.save(any(FichierRecette.class))).thenReturn(fichierRecette);
        when(minioService.getPresignedUrl(anyString(), anyString())).thenReturn("http://test-url.com");

        FichierRecetteDTO result = fichierRecetteService.uploadImage(1L, mockFile);

        assertNotNull(result);
        assertEquals("test.jpg", result.getNomOriginal());
        assertEquals(FichierRecette.TypeFichier.IMAGE, result.getType());
        verify(fichierRecetteRepository).save(any(FichierRecette.class));
    }

    @Test
    @DisplayName("Devrait lancer une exception si la recette n'existe pas")
    void testUploadImage_RecetteNotFound() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(recetteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            fichierRecetteService.uploadImage(1L, mockFile);
        });

        verify(fichierRecetteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait rejeter un type de fichier non autorisé pour l'image")
    void testUploadImage_InvalidFileType() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(recetteRepository.findById(1L)).thenReturn(Optional.of(recette));

        assertThrows(IllegalArgumentException.class, () -> {
            fichierRecetteService.uploadImage(1L, mockFile);
        });
    }

    @Test
    @DisplayName("Devrait uploader un document avec succès")
    void testUploadDocument_Success() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getSize()).thenReturn(1024L);

        fichierRecette.setType(FichierRecette.TypeFichier.DOCUMENT);
        fichierRecette.setContentType("application/pdf");

        when(recetteRepository.findById(1L)).thenReturn(Optional.of(recette));
        when(minioService.generateUniqueFileName(anyString())).thenReturn("unique-test.pdf");
        when(minioService.uploadFile(any(), anyString(), anyString())).thenReturn("unique-test.pdf");
        when(minioService.getDocumentsBucket()).thenReturn("documents-bucket");
        when(fichierRecetteRepository.save(any(FichierRecette.class))).thenReturn(fichierRecette);
        when(minioService.getPresignedUrl(anyString(), anyString())).thenReturn("http://test-url.com");

        FichierRecetteDTO result = fichierRecetteService.uploadDocument(1L, mockFile);

        assertNotNull(result);
        assertEquals(FichierRecette.TypeFichier.DOCUMENT, result.getType());
        verify(fichierRecetteRepository).save(any(FichierRecette.class));
    }

    @Test
    @DisplayName("Devrait récupérer tous les fichiers d'une recette")
    void testGetFichiersByRecette() {
        List<FichierRecette> fichiers = Arrays.asList(fichierRecette);
        when(fichierRecetteRepository.findByRecetteId(1L)).thenReturn(fichiers);
        when(minioService.getPresignedUrl(anyString(), anyString())).thenReturn("http://test-url.com");
        when(minioService.getRecettesBucket()).thenReturn("recettes-bucket");

        List<FichierRecetteDTO> result = fichierRecetteService.getFichiersByRecette(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test.jpg", result.get(0).getNomOriginal());
    }

    @Test
    @DisplayName("Devrait récupérer uniquement les images d'une recette")
    void testGetImagesByRecette() {
        List<FichierRecette> images = Arrays.asList(fichierRecette);
        when(fichierRecetteRepository.findByRecetteIdAndType(1L, FichierRecette.TypeFichier.IMAGE))
            .thenReturn(images);
        when(minioService.getPresignedUrl(anyString(), anyString())).thenReturn("http://test-url.com");
        when(minioService.getRecettesBucket()).thenReturn("recettes-bucket");

        List<FichierRecetteDTO> result = fichierRecetteService.getImagesByRecette(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(FichierRecette.TypeFichier.IMAGE, result.get(0).getType());
    }

    @Test
    @DisplayName("Devrait télécharger un fichier")
    void testDownloadFichier_Success() throws IOException {
        InputStream expectedStream = new ByteArrayInputStream("test".getBytes());
        when(fichierRecetteRepository.findById(1L)).thenReturn(Optional.of(fichierRecette));
        when(minioService.getRecettesBucket()).thenReturn("recettes-bucket");
        when(minioService.downloadFile(anyString(), anyString())).thenReturn(expectedStream);

        try (InputStream result = fichierRecetteService.downloadFichier(1L)) {
            assertNotNull(result);
        }
        verify(minioService).downloadFile(anyString(), anyString());
    }

    @Test
    @DisplayName("Devrait lancer une exception si le fichier à télécharger n'existe pas")
    void testDownloadFichier_NotFound() {
        when(fichierRecetteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fichierRecetteService.downloadFichier(1L));
    }

    @Test
    @DisplayName("Devrait supprimer un fichier")
    void testDeleteFichier_Success() {
        when(fichierRecetteRepository.findById(1L)).thenReturn(Optional.of(fichierRecette));
        when(minioService.getRecettesBucket()).thenReturn("recettes-bucket");
        doNothing().when(minioService).deleteFile(anyString(), anyString());
        doNothing().when(fichierRecetteRepository).delete(any(FichierRecette.class));

        assertDoesNotThrow(() -> {
            fichierRecetteService.deleteFichier(1L);
        });

        verify(minioService).deleteFile(anyString(), anyString());
        verify(fichierRecetteRepository).delete(any(FichierRecette.class));
    }

    @Test
    @DisplayName("Devrait supprimer tous les fichiers d'une recette")
    void testDeleteAllFichiersByRecette() {
        List<FichierRecette> fichiers = Arrays.asList(fichierRecette);
        when(fichierRecetteRepository.findByRecetteId(1L)).thenReturn(fichiers);
        when(minioService.getRecettesBucket()).thenReturn("recettes-bucket");
        doNothing().when(minioService).deleteFile(anyString(), anyString());
        doNothing().when(fichierRecetteRepository).deleteByRecetteId(1L);

        assertDoesNotThrow(() -> {
            fichierRecetteService.deleteAllFichiersByRecette(1L);
        });

        verify(minioService).deleteFile(anyString(), anyString());
        verify(fichierRecetteRepository).deleteByRecetteId(1L);
    }
}
