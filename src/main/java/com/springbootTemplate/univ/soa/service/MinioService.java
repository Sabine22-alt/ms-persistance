package com.springbootTemplate.univ.soa.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket.recettes}")
    private String recettesBucket;

    @Value("${minio.bucket.documents}")
    private String documentsBucket;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.public-url}")
    private String minioPublicUrl;

    /**
     * Génère l'URL publique d'un objet MinIO
     * Format: http://localhost:9002/bucket-name/object-path
     * @param bucketName le nom du bucket
     * @param objectPath le chemin de l'objet dans le bucket
     * @return l'URL publique complète
     */
    public String getPublicUrl(String bucketName, String objectPath) {
        return String.format("%s/%s/%s", minioPublicUrl, bucketName, objectPath);
    }

    public String uploadFile(MultipartFile file, String bucketName, String objectName) {
        try {
            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier: " + e.getMessage(), e);
        }
    }

    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du téléchargement du fichier: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier: " + e.getMessage(), e);
        }
    }

    public String getPresignedUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération de l'URL: " + e.getMessage(), e);
        }
    }

    public String generateUniqueFileName(String originalFilename) {
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }

    public boolean fileExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getRecettesBucket() {
        return recettesBucket;
    }

    public String getDocumentsBucket() {
        return documentsBucket;
    }
}

