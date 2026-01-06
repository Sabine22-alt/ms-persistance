package com.mspersistance.univ.soa.service;

import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests unitaires pour MinioService")
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioService minioService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioService, "recettesBucket", "recettes-bucket");
        ReflectionTestUtils.setField(minioService, "documentsBucket", "documents-bucket");
    }

    @Test
    @DisplayName("Devrait uploader un fichier avec succès")
    void testUploadFile_Success() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));

        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String result = minioService.uploadFile(mockFile, "test-bucket", "test-object.jpg");

        assertEquals("test-object.jpg", result);
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Devrait lancer une exception lors de l'échec de l'upload")
    void testUploadFile_Failure() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenThrow(new RuntimeException("Test exception"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            minioService.uploadFile(mockFile, "test-bucket", "test-object.jpg");
        });

        assertTrue(exception.getMessage().contains("Erreur lors de l'upload du fichier"));
    }

    @Test
    @DisplayName("Devrait télécharger un fichier avec succès")
    void testDownloadFile_Success() throws Exception {
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        InputStream result = minioService.downloadFile("test-bucket", "test-object.jpg");

        assertNotNull(result);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    @DisplayName("Devrait supprimer un fichier avec succès")
    void testDeleteFile_Success() throws Exception {
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertDoesNotThrow(() -> {
            minioService.deleteFile("test-bucket", "test-object.jpg");
        });

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("Devrait générer une URL présignée avec succès")
    void testGetPresignedUrl_Success() throws Exception {
        String expectedUrl = "http://localhost:9000/test-bucket/test-object.jpg?presigned=true";
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
            .thenReturn(expectedUrl);

        String result = minioService.getPresignedUrl("test-bucket", "test-object.jpg");

        assertEquals(expectedUrl, result);
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    @DisplayName("Devrait générer un nom de fichier unique")
    void testGenerateUniqueFileName() {
        String originalFilename = "test-image.jpg";

        String uniqueFilename = minioService.generateUniqueFileName(originalFilename);

        assertNotNull(uniqueFilename);
        assertTrue(uniqueFilename.endsWith(".jpg"));
        assertNotEquals(originalFilename, uniqueFilename);
    }

    @Test
    @DisplayName("Devrait vérifier qu'un fichier existe")
    void testFileExists_True() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class)))
            .thenReturn(mock(StatObjectResponse.class));

        boolean exists = minioService.fileExists("test-bucket", "test-object.jpg");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Devrait retourner false si le fichier n'existe pas")
    void testFileExists_False() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class)))
            .thenThrow(new RuntimeException("Not found"));

        boolean exists = minioService.fileExists("test-bucket", "test-object.jpg");

        assertFalse(exists);
    }

    @Test
    @DisplayName("Devrait retourner le bucket de recettes")
    void testGetRecettesBucket() {
        String bucket = minioService.getRecettesBucket();
        assertEquals("recettes-bucket", bucket);
    }

    @Test
    @DisplayName("Devrait retourner le bucket de documents")
    void testGetDocumentsBucket() {
        String bucket = minioService.getDocumentsBucket();
        assertEquals("documents-bucket", bucket);
    }
}

