package com.navigation.normalization_service.consumer;

import com.navigation.normalization_service.service.NormalizationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.kafka.clients.consumer.ConsumerRecord;

    @Component
    public class KafkaMessageConsumer {

        private final NormalizationService normalizationService;

        @Autowired
        public KafkaMessageConsumer(NormalizationService normalizationService) {
            this.normalizationService = normalizationService;
        }

        @KafkaListener(topics = "ingest.v1", groupId = "normalization-group")
        public void consume(ConsumerRecord<String, String> record) {
            String rawJson = record.value();
            System.out.println("📩 Consumed raw message from Kafka -> " + rawJson);

            // Pass the message to the normalization service for processing
            normalizationService.processAndStore(rawJson);
        }
    }

