package com.example.demo.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // Adjust host/port/scheme + auth headers if needed
        RestClient restClient = RestClient.builder(
                new org.apache.http.HttpHost("localhost", 9200, "http")
        ).setDefaultHeaders(new Header[]{
                // If you run ES 8 with security off, you can remove headers
                // new BasicHeader("Authorization", "ApiKey XYZ"),
        }).build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new co.elastic.clients.json.jackson.JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
