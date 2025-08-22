package com.pm.team1_dash_v.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    @Value("${elasticsearch.host:http://localhost:9200}")
    private String esUrl;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient lowLevel = RestClient.builder(HttpHost.create(esUrl)).build();
        ObjectMapper om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 strings

        ElasticsearchTransport transport =
                new RestClientTransport(lowLevel, new JacksonJsonpMapper(om));

        return new ElasticsearchClient(transport);
    }
}
