package com.example.demo.kafka;

import com.example.demo.model.NormalizedMessage;
import com.example.demo.search.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageConsumer {

    private final SearchService searchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaMessageConsumer(SearchService searchService) {
        this.searchService = searchService;
    }

    @KafkaListener(topics = "canonical.es.v1", groupId = "elasticsearch-indexer")  // Fixed topic name
    public void consume(ConsumerRecord<String, String> record) {
        try {
            System.out.println("Received message key: " + record.key());
            System.out.println("Received message value: " + record.value());

            String messageJson = record.value();
            NormalizedMessage message = objectMapper.readValue(messageJson, NormalizedMessage.class);

            searchService.indexMessage(message);
            System.out.println("Successfully indexed message: " + message.getMessageId());

        } catch (Exception e) {
            System.err.println("Failed to process message: " + record.value());
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}