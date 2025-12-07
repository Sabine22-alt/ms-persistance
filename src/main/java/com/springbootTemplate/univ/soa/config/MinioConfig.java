package com.springbootTemplate.univ.soa.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket.recettes}")
    private String recettesBucket;

    @Value("${minio.bucket.documents}")
    private String documentsBucket;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        try {
            createBucketIfNotExists(minioClient, recettesBucket);
            createBucketIfNotExists(minioClient, documentsBucket);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation de MinIO: " + e.getMessage(), e);
        }

        return minioClient;
    }

    private void createBucketIfNotExists(MinioClient minioClient, String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }
}

