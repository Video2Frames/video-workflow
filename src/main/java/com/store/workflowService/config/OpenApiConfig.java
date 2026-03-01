package com.store.workflowService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI openAPI(
                @Value("${spring.application.name:video-workflow}") String appName,
                @Value("${api.doc.version:v1}") String apiVersion,
                @Value("${api.doc.server-url:/}") String serverUrl
        ) {
                return new OpenAPI()
                        .info(new Info()
                                .title(appName + " API")
                                .version(apiVersion)
                                .description("API de upload de vídeos para S3 (AWS/LocalStack)"))
                        .servers(List.of(new Server().url(serverUrl)));
        }

        @PostConstruct
        public void logEnv() {
                System.out.println("### AWS_SNS_TOPIC_ARN env = " + System.getenv("AWS_SNS_TOPIC_ARN"));
        }
}