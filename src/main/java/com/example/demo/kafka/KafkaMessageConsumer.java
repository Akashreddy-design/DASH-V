// src/main/java/com/example/demo/kafka/KafkaMessageConsumer.java
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

    @KafkaListener(topics = "normalized.v1", groupId = "elasticsearch-indexer")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            String messageJson = record.value();
            NormalizedMessage message = objectMapper.readValue(messageJson, NormalizedMessage.class);
            searchService.indexMessage(message);
            System.out.println("Indexed message from Kafka: " + message.getMessageId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
