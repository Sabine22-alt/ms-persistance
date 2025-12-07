package com.springbootTemplate.univ.soa.controller;

import com.springbootTemplate.univ.soa.dto.FichierRecetteDTO;
import com.springbootTemplate.univ.soa.model.FichierRecette;
import com.springbootTemplate.univ.soa.service.FichierRecetteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FichierRecetteController.class)
@ActiveProfiles("test")
@DisplayName("Tests unitaires pour FichierRecetteController")
class FichierRecetteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FichierRecetteService fichierRecetteService;

    private FichierRecetteDTO fichierDTO;

    @BeforeEach
    void setUp() {
        fichierDTO = new FichierRecetteDTO();
        fichierDTO.setId(1L);
        fichierDTO.setNomOriginal("test.jpg");
        fichierDTO.setNomStocke("unique-test.jpg");
        fichierDTO.setContentType("image/jpeg");
        fichierDTO.setTaille(1024L);
        fichierDTO.setType(FichierRecette.TypeFichier.IMAGE);
        fichierDTO.setRecetteId(1L);
        fichierDTO.setUrlTelechargement("http://test-url.com");
        fichierDTO.setDateUpload(LocalDateTime.now());
    }

    @Test
    @DisplayName("Devrait uploader une image avec succès")
    void testUploadImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        when(fichierRecetteService.uploadImage(eq(1L), any())).thenReturn(fichierDTO);

        mockMvc.perform(multipart("/api/persistance/recettes/1/fichiers/images")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nomOriginal").value("test.jpg"))
                .andExpect(jsonPath("$.type").value("IMAGE"));

        verify(fichierRecetteService).uploadImage(eq(1L), any());
    }

    @Test
    @DisplayName("Devrait retourner une erreur si le fichier est vide lors de l'upload d'image")
    void testUploadImage_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            new byte[0]
        );

        mockMvc.perform(multipart("/api/persistance/recettes/1/fichiers/images")
                .file(file))
                .andExpect(status().isBadRequest());

        verify(fichierRecetteService, never()).uploadImage(anyLong(), any());
    }

    @Test
    @DisplayName("Devrait uploader un document avec succès")
    void testUploadDocument_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "test pdf content".getBytes()
        );

        fichierDTO.setType(FichierRecette.TypeFichier.DOCUMENT);
        fichierDTO.setContentType("application/pdf");

        when(fichierRecetteService.uploadDocument(eq(1L), any())).thenReturn(fichierDTO);

        mockMvc.perform(multipart("/api/persistance/recettes/1/fichiers/documents")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DOCUMENT"));

        verify(fichierRecetteService).uploadDocument(eq(1L), any());
    }

    @Test
    @DisplayName("Devrait récupérer tous les fichiers d'une recette")
    void testGetAllFichiers() throws Exception {
        List<FichierRecetteDTO> fichiers = Arrays.asList(fichierDTO);
        when(fichierRecetteService.getFichiersByRecette(1L)).thenReturn(fichiers);

        mockMvc.perform(get("/api/persistance/recettes/1/fichiers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nomOriginal").value("test.jpg"));

        verify(fichierRecetteService).getFichiersByRecette(1L);
    }

    @Test
    @DisplayName("Devrait récupérer uniquement les images d'une recette")
    void testGetImages() throws Exception {
        List<FichierRecetteDTO> images = Arrays.asList(fichierDTO);
        when(fichierRecetteService.getImagesByRecette(1L)).thenReturn(images);

        mockMvc.perform(get("/api/persistance/recettes/1/fichiers/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("IMAGE"));

        verify(fichierRecetteService).getImagesByRecette(1L);
    }

    @Test
    @DisplayName("Devrait récupérer uniquement les documents d'une recette")
    void testGetDocuments() throws Exception {
        fichierDTO.setType(FichierRecette.TypeFichier.DOCUMENT);
        List<FichierRecetteDTO> documents = Arrays.asList(fichierDTO);
        when(fichierRecetteService.getDocumentsByRecette(1L)).thenReturn(documents);

        mockMvc.perform(get("/api/persistance/recettes/1/fichiers/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("DOCUMENT"));

        verify(fichierRecetteService).getDocumentsByRecette(1L);
    }

    @Test
    @DisplayName("Devrait télécharger un fichier")
    void testDownloadFichier() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        when(fichierRecetteService.getFichierById(1L)).thenReturn(fichierDTO);
        when(fichierRecetteService.downloadFichier(1L)).thenReturn(inputStream);

        mockMvc.perform(get("/api/persistance/recettes/1/fichiers/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));

        verify(fichierRecetteService).downloadFichier(1L);
    }

    @Test
    @DisplayName("Devrait récupérer les métadonnées d'un fichier")
    void testGetFichierMetadata() throws Exception {
        when(fichierRecetteService.getFichierById(1L)).thenReturn(fichierDTO);

        mockMvc.perform(get("/api/persistance/recettes/1/fichiers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nomOriginal").value("test.jpg"));

        verify(fichierRecetteService).getFichierById(1L);
    }

    @Test
    @DisplayName("Devrait supprimer un fichier")
    void testDeleteFichier() throws Exception {
        doNothing().when(fichierRecetteService).deleteFichier(1L);

        mockMvc.perform(delete("/api/persistance/recettes/1/fichiers/1"))
                .andExpect(status().isNoContent());

        verify(fichierRecetteService).deleteFichier(1L);
    }

    @Test
    @DisplayName("Devrait supprimer tous les fichiers d'une recette")
    void testDeleteAllFichiers() throws Exception {
        doNothing().when(fichierRecetteService).deleteAllFichiersByRecette(1L);

        mockMvc.perform(delete("/api/persistance/recettes/1/fichiers"))
                .andExpect(status().isNoContent());

        verify(fichierRecetteService).deleteAllFichiersByRecette(1L);
    }

    @Test
    @DisplayName("Devrait gérer les erreurs lors de l'upload")
    void testUploadImage_WithError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        when(fichierRecetteService.uploadImage(eq(1L), any()))
            .thenThrow(new IllegalArgumentException("Type de fichier non autorisé"));

        mockMvc.perform(multipart("/api/persistance/recettes/1/fichiers/images")
                .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Devrait gérer les erreurs lors du téléchargement")
    void testDownloadFichier_WithError() throws Exception {
        when(fichierRecetteService.getFichierById(1L))
            .thenThrow(new RuntimeException("Erreur interne"));

        mockMvc.perform(get("/api/persistance/recettes/1/fichiers/1/download"))
                .andExpect(status().isInternalServerError());
    }
}

