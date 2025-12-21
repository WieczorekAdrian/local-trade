package io.github.adrian.wieczorek.local_trade.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class AwsS3Config {

    @Value("${s3.useMinio:true}")
    private boolean useMinio;

    @Value("${s3.endpoint:http://localhost:9000}")
    private String minioEndpoint;

    @Value("${s3.accessKey:minioadmin}")
    private String minioAccessKey;

    @Value("${s3.secretKey:minioadmin}")
    private String minioSecretKey;

    @Value("${aws.region:eu-central-1}")
    private String awsRegion;

    @Bean
    public S3Client s3Client() {
        if (useMinio) {
            return S3Client.builder()
                    .endpointOverride(URI.create(minioEndpoint))
                    .region(Region.of("us-east-1"))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(minioAccessKey, minioSecretKey)
                    ))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build())
                    .build();
        } else {
            return S3Client.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .build();
        }
    }

    @Bean
    public S3Presigner s3Presigner() {
        if (useMinio) {
            return S3Presigner.builder()
                    .endpointOverride(URI.create(minioEndpoint))
                    .region(Region.of("us-east-1"))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(minioAccessKey, minioSecretKey)
                    ))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build())
                    .build();
        } else {
            return S3Presigner.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .build();
        }
    }
}