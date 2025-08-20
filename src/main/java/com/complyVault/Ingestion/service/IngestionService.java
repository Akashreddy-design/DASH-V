package com.complyVault.Ingestion.service;

import com.complyVault.Ingestion.model.EmailMessageRaw;
import com.complyVault.Ingestion.model.IngestEvent;
import com.complyVault.Ingestion.model.SlackMessageRaw;
import com.complyVault.Ingestion.model.audits.AuditLog;
import com.complyVault.Ingestion.repository.AuditLogRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

@Service
public class IngestionService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AuditLogRepository auditLogRepository;
    private static final String TOPIC = "ingest.v1";

    public IngestionService(KafkaTemplate<String, Object> kafkaTemplate, AuditLogRepository auditLogRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.auditLogRepository = auditLogRepository;
    }

    // --- Handle Email ---
    public String ingestEmail(EmailMessageRaw email) {
        // 1. Validate
        validateEmail(email);

        // 2. Generate MessageId
        String messageId = generateMessageId(email.getTenantId(),
                email.getPayload().getFrom(),
                email.getPayload().getSentAt().toString());

        AuditLog auditLog = new AuditLog();
        auditLog.setTenantId(email.getTenantId());
        auditLog.setMessageId(messageId);
        auditLog.setEventType("EMAIL_INGESTED");
        auditLog.setNetwork(email.getNetwork());
        auditLog.setTimestamp(Instant.now());
        auditLogRepository.save(auditLog);

        // 4. Publish to Kafka (TODO next step)
        // publishIngestEvent(messageId, email);

        // Build IngestEvent
        IngestEvent event = new IngestEvent(messageId,
                email.getTenantId(),
                email.getNetwork(),
                email,
                Instant.now());

        // Publish to Kafka
        kafkaTemplate.send(TOPIC, messageId, event);


        return messageId;
    }

    // --- Handle Slack ---
    public String ingestSlack(SlackMessageRaw slack) {
        // 1. Validate
        validateSlack(slack);

        // 2. Use existing messageId if present, else generate
        String messageId = StringUtils.hasText(slack.getMessageId())
                ? slack.getMessageId()
                : generateMessageId(slack.getTenantId(), slack.getUser(), slack.getTimestamp().toString());

        // 3. Log Audit to MongoDB
        // 3. Log Audit to MongoDB
        AuditLog auditLog = new AuditLog();
        auditLog.setTenantId(slack.getTenantId());
        auditLog.setMessageId(messageId);
        auditLog.setEventType("SLACK_INGESTED");
        auditLog.setNetwork(slack.getNetwork());
        auditLog.setTimestamp(Instant.now());
        auditLogRepository.save(auditLog);



        // 4. Publish to Kafka (TODO next step)
        // publishIngestEvent(messageId, slack);

        // Build IngestEvent
        IngestEvent event = new IngestEvent(messageId,
                slack.getTenantId(),
                slack.getNetwork(),
                slack,
                Instant.now());

        // Publish to Kafka
        kafkaTemplate.send(TOPIC, messageId, event);

        return messageId;
    }

    // --- Validation ---
    private void validateEmail(EmailMessageRaw email) {
        if (email == null || email.getPayload() == null)
            throw new IllegalArgumentException("Invalid Email JSON: missing payload");

        if (!StringUtils.hasText(email.getTenantId()))
            throw new IllegalArgumentException("Invalid Email JSON: missing tenantId");

        if (!StringUtils.hasText(email.getPayload().getFrom()))
            throw new IllegalArgumentException("Invalid Email JSON: missing 'from' field");

        if (email.getPayload().getSentAt() == null)
            throw new IllegalArgumentException("Invalid Email JSON: missing 'sentAt'");
    }

    private void validateSlack(SlackMessageRaw slack) {
        if (slack == null)
            throw new IllegalArgumentException("Invalid Slack JSON");

        if (!StringUtils.hasText(slack.getTenantId()))
            throw new IllegalArgumentException("Invalid Slack JSON: missing tenantId");

        if (!StringUtils.hasText(slack.getUser()))
            throw new IllegalArgumentException("Invalid Slack JSON: missing user");

        if (slack.getTimestamp() == null)
            throw new IllegalArgumentException("Invalid Slack JSON: missing timestamp");
    }

    // --- Stable MessageId generator ---
    private String generateMessageId(String tenantId, String sender, String timestamp) {
        String base = tenantId + "|" + sender + "|" + timestamp;
        return UUID.nameUUIDFromBytes(base.getBytes()).toString(); // stable UUID
    }
}
