package com.complyVault.Ingestion.service;

import com.complyVault.Ingestion.model.EmailMessageRaw;
import com.complyVault.Ingestion.model.IngestEvent;
import com.complyVault.Ingestion.model.SlackMessageRaw;
import com.complyVault.Ingestion.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for IngestionService.
 * This class uses Mockito to test the business logic of the service in isolation,
 * mocking its dependency on KafkaTemplate.
 */
@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    // @Mock creates a mock instance of KafkaTemplate.
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    // @Mock also creates a mock for the AuditLogRepository.
    @Mock
    private AuditLogRepository auditLogRepository;

    // @InjectMocks creates an instance of IngestionService and injects
    // the mock KafkaTemplate and AuditLogRepository into it.
    @InjectMocks
    private IngestionService ingestionService;

    @Test
    @DisplayName("Should ingest a valid email and publish to Kafka")
    void ingestEmail_ValidData_PublishesToKafka() {
        // Arrange
        EmailMessageRaw email = new EmailMessageRaw();
        email.setTenantId("test-tenant-1");
        email.setNetwork("email");
        EmailMessageRaw.EmailPayload payload = new EmailMessageRaw.EmailPayload();
        payload.setFrom("user@example.com");
        payload.setTo(Collections.singletonList("recipient@example.com"));
        payload.setSubject("Hello");
        payload.setBody("Test email body");
        payload.setSentAt(Instant.now());
        email.setPayload(payload);

        // Act
        String messageId = ingestionService.ingestEmail(email);

        // Assert
        // Verify that the service returns a non-null, non-empty messageId.
        assertNotNull(messageId);
        assertFalse(messageId.isEmpty());

        // Capture the arguments passed to kafkaTemplate.send to verify their correctness.
        ArgumentCaptor<IngestEvent> eventCaptor = ArgumentCaptor.forClass(IngestEvent.class);
        verify(kafkaTemplate).send(eq("ingest.v1"), eq(messageId), eventCaptor.capture());

        // Verify the captured IngestEvent object.
        IngestEvent capturedEvent = eventCaptor.getValue();
        assertEquals(messageId, capturedEvent.getMessageId());
        assertEquals("test-tenant-1", capturedEvent.getTenantId());
        assertEquals("email", capturedEvent.getNetwork());
        assertEquals(email, capturedEvent.getRawPayload());
        assertNotNull(capturedEvent.getIngestedAt());
    }

    @Test
    @DisplayName("Should ingest a valid slack message and publish to Kafka")
    void ingestSlack_ValidData_PublishesToKafka() {
        // Arrange
        SlackMessageRaw slack = new SlackMessageRaw();
        slack.setTenantId("test-tenant-2");
        slack.setNetwork("slack");
        slack.setUser("slack-user");
        slack.setTimestamp(Instant.now());
        slack.setText("Test slack message");

        // Act
        String messageId = ingestionService.ingestSlack(slack);

        // Assert
        assertNotNull(messageId);

        // Verify that the messageId is a generated UUID and not null.
        // It should not be the 'messageId' from the input since we didn't provide one.
        assertEquals(36, messageId.length());

        ArgumentCaptor<IngestEvent> eventCaptor = ArgumentCaptor.forClass(IngestEvent.class);
        verify(kafkaTemplate).send(eq("ingest.v1"), eq(messageId), eventCaptor.capture());

        // Verify the captured event details.
        IngestEvent capturedEvent = eventCaptor.getValue();
        assertEquals(messageId, capturedEvent.getMessageId());
        assertEquals("test-tenant-2", capturedEvent.getTenantId());
        assertEquals("slack", capturedEvent.getNetwork());
        assertEquals(slack, capturedEvent.getRawPayload());
    }

    @Test
    @DisplayName("Should use existing messageId from Slack payload")
    void ingestSlack_WithExistingMessageId_UsesIt() {
        // Arrange
        String existingMessageId = "pre-existing-id-123";
        SlackMessageRaw slack = new SlackMessageRaw();
        slack.setMessageId(existingMessageId); // set existing ID
        slack.setTenantId("test-tenant-3");
        slack.setNetwork("slack");
        slack.setUser("slack-user-2");
        slack.setTimestamp(Instant.now());
        slack.setText("Another slack message");

        // Act
        String returnedMessageId = ingestionService.ingestSlack(slack);

        // Assert
        // The returned messageId should be the one from the input payload.
        assertEquals(existingMessageId, returnedMessageId);

        // Verify that Kafka was called with the correct existing message ID.
        ArgumentCaptor<IngestEvent> eventCaptor = ArgumentCaptor.forClass(IngestEvent.class);
        verify(kafkaTemplate).send(eq("ingest.v1"), eq(existingMessageId), eventCaptor.capture());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid email payload")
    void ingestEmail_InvalidPayload_ThrowsException() {
        // Arrange
        EmailMessageRaw email = new EmailMessageRaw();
        // missing payload, tenantId, from, sentAt
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ingestionService.ingestEmail(email),
                "Should throw exception for null email payload");

        email.setPayload(new EmailMessageRaw.EmailPayload());
        assertThrows(IllegalArgumentException.class, () -> ingestionService.ingestEmail(email),
                "Should throw exception for missing tenantId");

        email.setTenantId("tenant1");
        assertThrows(IllegalArgumentException.class, () -> ingestionService.ingestEmail(email),
                "Should throw exception for missing 'from'");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid slack payload")
    void ingestSlack_InvalidPayload_ThrowsException() {
        // Arrange
        SlackMessageRaw slack = new SlackMessageRaw();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ingestionService.ingestSlack(slack),
                "Should throw exception for missing tenantId");

        slack.setTenantId("tenant1");
        assertThrows(IllegalArgumentException.class, () -> ingestionService.ingestSlack(slack),
                "Should throw exception for missing user");
    }
}
