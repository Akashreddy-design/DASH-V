package com.dharani.ingestion.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "ingest.v1", groupId = "ingestion-group")
    public void consume(ConsumerRecord<String, String> record) {
        System.out.println("📩 Consumed message from Kafka -> " + record.value());
    }
}
