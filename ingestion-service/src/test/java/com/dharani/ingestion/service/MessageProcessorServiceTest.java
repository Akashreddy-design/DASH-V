package com.dharani.ingestion.service;
import com.dharani.ingestion.service.MessageProcessorService;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;

public class MessageProcessorServiceTest {

    private KafkaTemplate<String, String> mockKafka;
    private MessageProcessorService service;

    @BeforeEach
    void setup() {
        // Create a mock KafkaTemplate
        mockKafka = Mockito.mock(KafkaTemplate.class);
        service = new MessageProcessorService(mockKafka);
    }

    @Test
    void testValidEmailJson() {
        String validEmail = """
            {
              "tenantId": "bank-001",
              "network": "email",
              "payload": {
                "from": "user@bank.com",
                "to": ["analyst@bank.com"],
                "subject": "Hello",
                "body": "Test message",
                "sentAt": "2025-08-10T10:15:00Z"
              }
            }
            """;

        assertDoesNotThrow(() -> service.processMessage(validEmail, "email"));
    }

    @Test
    void testInvalidEmailJsonMissingField() {
        String invalidEmail = """
            {
              "tenantId": "bank-001",
              "network": "email",
              "payload": {
                "from": "user@bank.com"
              }
            }
            """;

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.processMessage(invalidEmail, "email"));
        assertTrue(ex.getMessage().contains("JSON validation failed"));
    }

    @Test
    void testValidSlackJson() {
        String validSlack = """
            {
              "tenantId": "bank-002",
              "network": "slack",
              "user": "trader2",
              "text": "Hello Slack",
              "timestamp": "2025-08-14T08:45:12Z",
              "team": "trading-floor",
              "channel": "general"
            }
            """;

        assertDoesNotThrow(() -> service.processMessage(validSlack, "slack"));
    }

    @Test
    void testInvalidSlackJsonMissingUser() {
        String invalidSlack = """
            {
              "tenantId": "bank-002",
              "network": "slack",
              "text": "Message without user",
              "timestamp": "2025-08-14T08:45:12Z",
              "team": "trading-floor",
              "channel": "general"
            }
            """;

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.processMessage(invalidSlack, "slack"));
        assertTrue(ex.getMessage().contains("JSON validation failed"));
    }
}
