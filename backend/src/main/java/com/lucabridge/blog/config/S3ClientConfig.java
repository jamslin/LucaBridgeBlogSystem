package com.lucabridge.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * S3-compatible client wired to MinIO locally; swap STORAGE_ENDPOINT to AWS S3 in prod
 * with no code change (see 04 · Engineering Setup & Roadmap).
 */
@Configuration
public class S3ClientConfig {

    @Bean
    public S3Client s3Client(AppProperties props) {
        AppProperties.Storage storage = props.getStorage();
        return S3Client.builder()
                .endpointOverride(URI.create(storage.getEndpoint()))
                .region(Region.of(storage.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(storage.getAccessKey(), storage.getSecretKey())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // required for MinIO
                        .build())
                .build();
    }
}
