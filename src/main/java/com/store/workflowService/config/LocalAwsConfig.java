package com.store.workflowService.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

@Configuration
@Profile("local")
public class LocalAwsConfig {

    private static final Logger log = LoggerFactory.getLogger(LocalAwsConfig.class);

    @Bean
    public S3Client s3Client(
            @Value("${aws.region:us-east-1}") String region,
            @Value("${aws.s3.endpoint:}") String endpoint,
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey,
            @Value("${aws.s3.path-style-access:true}") boolean pathStyleAccess
    ) {

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                );

        if (endpoint != null && !endpoint.isBlank()) {
            log.info("Configuring S3 client with endpoint override: {}", endpoint);
            builder = builder.endpointOverride(URI.create(endpoint));
        } else {
            log.info("No S3 endpoint override configured; using AWS default for region {}", region);
        }

        // Apply path-style addressing only when explicitly requested to avoid double-configuration
        if (pathStyleAccess) {
            S3Configuration s3Config = S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build();

            builder.serviceConfiguration(s3Config);
        } else {
            log.info("S3 path-style access disabled; using virtual-hosted style");
        }

        return builder.build();
    }

    @Bean
    public SnsClient snsClient(
            @Value("${aws.region:us-east-1}") String region,
            @Value("${aws.sns.endpoint:}") String endpoint,
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey
    ) {

        SnsClientBuilder builder = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                );

        if (endpoint != null && !endpoint.isBlank()) {
            log.info("Configuring SNS client with endpoint override: {}", endpoint);
            builder = builder.endpointOverride(URI.create(endpoint));
        } else {
            log.info("No SNS endpoint override configured; using AWS default for region {}", region);
        }

        return builder.build();
    }

    @Bean
    public SqsClient sqsClient(
            @Value("${aws.region:us-east-1}") String region,
            @Value("${aws.sqs.endpoint:}") String endpoint,
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey
    ) {
        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                );

        if (endpoint != null && !endpoint.isBlank()) {
            log.info("Configuring SQS client with endpoint override: {}", endpoint);
            builder = builder.endpointOverride(URI.create(endpoint));
        } else {
            log.info("No SQS endpoint override configured; using AWS default for region {}", region);
        }

        return builder.build();
    }
}