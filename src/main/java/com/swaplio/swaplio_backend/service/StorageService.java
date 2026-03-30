// service/StorageService.java
package com.swaplio.swaplio_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
public class StorageService {

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    @Value("${supabase.storage.url}")
    private String storageUrl;

    @Value("${supabase.storage.access-key}")
    private String accessKey;

    @Value("${supabase.storage.secret-key}")
    private String secretKey;

    @Value("${supabase.storage.region}")
    private String region;

    // We build the presigner manually (can't inject as @Bean easily with dynamic values)
    public StorageService(S3Client s3Client,
                          @Value("${supabase.storage.url}") String storageUrl,
                          @Value("${supabase.storage.access-key}") String accessKey,
                          @Value("${supabase.storage.secret-key}") String secretKey,
                          @Value("${supabase.storage.region}") String region,
                          @Value("${supabase.storage.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.storageUrl = storageUrl;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.bucket = bucket;

        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(storageUrl))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .region(Region.of(region))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    /**
     * Uploads image, stores only the file KEY (not full URL) in the database
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        String fileKey = folder + "/" + UUID.randomUUID() + "." + extension;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );

        // Return only the KEY, not a full URL — we generate URLs on demand
        return fileKey;
    }

    /**
     * Generates a signed URL valid for the given duration
     * Call this whenever Flutter needs to display an image
     */
    public String generateSignedUrl(String fileKey, Duration expiresIn) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiresIn)
                .getObjectRequest(r -> r.bucket(bucket).key(fileKey))
                .build();

        return presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    /**
     * Convenience method — default 1 hour expiry
     */
    public String generateSignedUrl(String fileKey) {
        return generateSignedUrl(fileKey, Duration.ofHours(1));
    }

    public void deleteImage(String fileKey) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .build()
        );
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}