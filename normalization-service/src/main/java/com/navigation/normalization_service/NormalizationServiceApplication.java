package com.navigation.normalization_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "com.navigation.normalization_service.store")
@EntityScan(basePackages = "com.navigation.normalization_service.model")

@SpringBootApplication
//@EnableElasticsearchRepositories(basePackages = "com.navigation.normalization_service.store")
class NormalizationApplication {
    public static void main(String[] args) {
        SpringApplication.run(NormalizationApplication.class, args);
    }
}
