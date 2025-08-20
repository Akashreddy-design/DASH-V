package com.complyVault.Ingestion.service;

import com.complyVault.Ingestion.model.EmailMessageRaw;
import com.complyVault.Ingestion.model.SlackMessageRaw;
import com.complyVault.Ingestion.model.audits.AuditLog;
import com.complyVault.Ingestion.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Integration tests for IngestionService using an in-memory MongoDB database.
 * This test class uses an embedded MongoDB instance for testing,
 * which does not require a running Docker environment.
 */
@DataMongoTest
@Import({IngestionService.class, IngestionServiceMongoTest.TestConfig.class})
class IngestionServiceMongoTest {

    @Autowired
    private IngestionService ingestionService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public KafkaTemplate<String, Object> kafkaTemplate() {
            // Provide a mock KafkaTemplate bean for the test context
            return mock(KafkaTemplate.class);
        }
    }

    /**
     * Cleans the audit log repository before each test method runs.
     * This ensures that each test has a clean database state and can run independently.
     * The previous test failure was due to this cleanup step being missing.
     */
    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save an audit log to MongoDB when an email is ingested")
    void ingestEmail_SavesAuditLog() {
        // Arrange
        EmailMessageRaw email = new EmailMessageRaw();
        email.setTenantId("test-tenant-123");
        email.setNetwork("email");
        EmailMessageRaw.EmailPayload payload = new EmailMessageRaw.EmailPayload();
        payload.setFrom("user@example.com");
        payload.setTo(Collections.singletonList("recipient@example.com"));
        payload.setSubject("Test Subject");
        payload.setBody("Test Body");
        payload.setSentAt(Instant.now());
        email.setPayload(payload);

        // Act
        ingestionService.ingestEmail(email);

        // Assert
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertFalse(auditLogs.isEmpty(), "Audit log list should not be empty");
        assertEquals(1, auditLogs.size(), "There should be one audit log entry");

        AuditLog savedAuditLog = auditLogs.get(0);
        assertNotNull(savedAuditLog.getId(), "Audit log ID should be generated");
        assertEquals("test-tenant-123", savedAuditLog.getTenantId());
        assertEquals("EMAIL_INGESTED", savedAuditLog.getEventType());
        assertEquals("email", savedAuditLog.getNetwork());
        assertNotNull(savedAuditLog.getTimestamp(), "Timestamp should not be null");
    }

    @Test
    @DisplayName("Should save an audit log to MongoDB when a slack message is ingested")
    void ingestSlack_SavesAuditLog() {
        // Arrange
        SlackMessageRaw slack = new SlackMessageRaw();
        slack.setTenantId("test-tenant-456");
        slack.setNetwork("slack");
        slack.setUser("test-user");
        slack.setTimestamp(Instant.now());
        slack.setText("Test slack message");

        // Act
        ingestionService.ingestSlack(slack);

        // Assert
        List<AuditLog> auditLogs = auditLogRepository.findAll();
        assertFalse(auditLogs.isEmpty(), "Audit log list should not be empty");
        assertEquals(1, auditLogs.size(), "There should be one audit log entry");

        AuditLog savedAuditLog = auditLogs.get(0);
        assertNotNull(savedAuditLog.getId(), "Audit log ID should be generated");
        assertEquals("test-tenant-456", savedAuditLog.getTenantId());
        assertEquals("SLACK_INGESTED", savedAuditLog.getEventType());
        assertEquals("slack", savedAuditLog.getNetwork());
        assertNotNull(savedAuditLog.getTimestamp(), "Timestamp should not be null");
    }
}
