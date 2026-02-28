package com.store.workflowService.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AwsS3Properties.class)
public class S3PropertiesConfig {
}
