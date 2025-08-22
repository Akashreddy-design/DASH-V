package com.pm.team1_dash_v.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafka;

    public KafkaProducerService(KafkaTemplate<String, String> kafka) {
        this.kafka = kafka;
    }

    public void send(String topic, String key, String value, Header... headers) {
        ProducerRecord<String, String> rec = new ProducerRecord<>(topic, null, key, value, List.of(headers));
        kafka.send(rec);
    }
}
