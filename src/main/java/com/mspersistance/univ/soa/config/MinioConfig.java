package com.mspersistance.univ.soa.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
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

            setPublicReadPolicy(minioClient, recettesBucket);
            System.out.println("MinIO configurÃ© : bucket '" + recettesBucket + "' en accÃ¨s public");
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation de MinIO: " + e.getMessage(), e);
        }

        return minioClient;
    }

    private void createBucketIfNotExists(MinioClient minioClient, String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            System.out.println("ðŸ“¦ Bucket crÃ©Ã© : " + bucketName);
        }
    }

    /**
     * DÃ©finir une politique d'accÃ¨s public (lecture seule) pour un bucket.
     * Permet d'accÃ©der aux images via http://localhost:9002/bucket/object
     */
    private void setPublicReadPolicy(MinioClient minioClient, String bucketName) throws Exception {
        // Politique JSON pour accÃ¨s public en lecture seule
        String policy = String.format(
            "{\"Version\":\"2012-10-17\"," +
            "\"Statement\":[{" +
            "\"Effect\":\"Allow\"," +
            "\"Principal\":{\"AWS\":[\"*\"]}," +
            "\"Action\":[\"s3:GetObject\"]," +
            "\"Resource\":[\"arn:aws:s3:::%s/*\"]" +
            "}]}",
            bucketName
        );

        minioClient.setBucketPolicy(
            SetBucketPolicyArgs.builder()
                .bucket(bucketName)
                .config(policy)
                .build()
        );

        System.out.println("ðŸ”“ Politique d'accÃ¨s public configurÃ©e pour : " + bucketName);
    }
}


