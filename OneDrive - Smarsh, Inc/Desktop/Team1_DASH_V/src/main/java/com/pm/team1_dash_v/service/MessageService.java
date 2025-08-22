package com.pm.team1_dash_v.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.team1_dash_v.interfaces.StorageServiceInterface;
import com.pm.team1_dash_v.kafka.KafkaProducerService;
import com.pm.team1_dash_v.model.AuditLogs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageService {

    private final KafkaProducerService producer;
    private final StorageServiceInterface storage;
    private final AuditService audit;
    private final SchemaValidator schemaValidator;   // ✅ inject here
    private final ObjectMapper M = new ObjectMapper();

    @Value("${cv.kafka.topic.email}")
    private String emailTopic;

    @Value("${cv.kafka.topic.slack}")
    private String slackTopic;

    public MessageService(KafkaProducerService producer,
                          StorageServiceInterface storage,
                          AuditService audit,
                          SchemaValidator schemaValidator) {
        this.producer = producer;
        this.storage = storage;
        this.audit = audit;
        this.schemaValidator = schemaValidator;
    }

    public IngestResponse ingestEmail(JsonNode body) {
        try {
            schemaValidator.validate("schemas/email.schema.json", body);   // ✅ instance call
            audit.log(AuditLogs.builder()
                    .logId(UUID.randomUUID().toString())
                    .stage("Validation")
                    .service("MessageService")
                    .messageId("pending")
                    .status("passed")
                    .build());
        } catch (IllegalArgumentException ex) {
            audit.log(AuditLogs.builder()
                    .logId(UUID.randomUUID().toString())
                    .stage("Validation")
                    .service("MessageService")
                    .messageId("n/a")
                    .status("failed")
                    .errors(List.of(ex.getMessage()))
                    .build());
            throw ex;
        }

        String tenantId = body.get("tenantId").asText();
        JsonNode p = body.get("payload");
        String msgId = StableId.forEmail(
                tenantId,
                p.get("from").asText(),
                M.convertValue(p.get("to"), new TypeReference<List<String>>() {}),
                p.path("subject").asText(""),
                p.path("body").asText(""),
                p.get("sentAt").asText()
        );

        storage.append("email", tenantId, msgId, body.toString());
        audit.log(AuditLogs.builder()
                .logId(UUID.randomUUID().toString())
                .stage("Storage")
                .service("MessageService")
                .messageId(msgId)
                .status("passed")
                .details(Map.of("bucket", "email", "tenantId", tenantId))
                .build());

        producer.send(emailTopic, msgId, body.toString(),
                header("tenantId", tenantId),
                header("network", "email"),
                header("messageId", msgId));
        audit.log(AuditLogs.builder()
                .logId(UUID.randomUUID().toString())
                .stage("KafkaProduce")
                .service("MessageService")
                .messageId(msgId)
                .status("passed")
                .details(Map.of("topic", emailTopic))
                .build());

        return new IngestResponse(msgId, "accepted");
    }

    public IngestResponse ingestSlack(JsonNode body) {
        try {
            schemaValidator.validate("schemas/slack.schema.json", body);  // ✅ instance call
            audit.log(AuditLogs.builder()
                    .logId(UUID.randomUUID().toString())
                    .stage("Validation")
                    .service("MessageService")
                    .messageId("pending")
                    .status("passed")
                    .build());
        } catch (IllegalArgumentException ex) {
            audit.log(AuditLogs.builder()
                    .logId(UUID.randomUUID().toString())
                    .stage("Validation")
                    .service("MessageService")
                    .messageId("n/a")
                    .status("failed")
                    .errors(List.of(ex.getMessage()))
                    .build());
            throw ex;
        }

        String tenantId = body.get("tenantId").asText();
        String provided = body.has("messageId") ? body.get("messageId").asText() : null;
        String msgId = (provided != null && !provided.isBlank())
                ? provided
                : StableId.forSlack(
                tenantId,
                body.get("user").asText(),
                body.get("text").asText(),
                body.get("timestamp").asText(),
                body.get("team").asText(),
                body.get("channel").asText()
        );

        storage.append("slack", tenantId, msgId, body.toString());
        audit.log(AuditLogs.builder()
                .logId(UUID.randomUUID().toString())
                .stage("Storage")
                .service("MessageService")
                .messageId(msgId)
                .status("passed")
                .details(Map.of("bucket", "slack", "tenantId", tenantId))
                .build());

        producer.send(slackTopic, msgId, body.toString(),
                header("tenantId", tenantId),
                header("network", "slack"),
                header("messageId", msgId));
        audit.log(AuditLogs.builder()
                .logId(UUID.randomUUID().toString())
                .stage("KafkaProduce")
                .service("MessageService")
                .messageId(msgId)
                .status("passed")
                .details(Map.of("topic", slackTopic))
                .build());

        return new IngestResponse(msgId, "accepted");
    }

    private static org.apache.kafka.common.header.Header header(String k, String v) {
        return new org.apache.kafka.common.header.internals.RecordHeader(k, v.getBytes());
    }

    public record IngestResponse(String messageId, String status) {}
}
