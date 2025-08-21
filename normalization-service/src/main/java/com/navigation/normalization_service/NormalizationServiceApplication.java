package com.navigation.normalization_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@EnableElasticsearchRepositories(basePackages = "com.navigation.normalization_service.store")
@SpringBootApplication
public class NormalizationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NormalizationServiceApplication.class, args);
    }
}
