// config/SupabaseStorageConfig.java
package com.swaplio.swaplio_backend.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import java.net.URI;

@Configuration
public class SupabaseStorageConfig {

    @Value("${supabase.storage.url}")
    private String storageUrl;

    @Value("${supabase.storage.access-key}")
    private String accessKey;

    @Value("${supabase.storage.secret-key}")
    private String secretKey;

    @Value("${supabase.storage.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(storageUrl))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .region(Region.of(region))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // Required for Supabase
                        .build())
                .build();
    }
}