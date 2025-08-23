package com.navigation.normalization_service.service;

import com.navigation.normalization_service.model.AuditLogs;
import com.navigation.normalization_service.store.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repo;

    /**
     * High level logging API: builds a nice message and persists to Mongo.
     */
    public void log(AuditLogs in) {
        // Ensure identifiers exist
        final String logId = in.getLogId() != null ? in.getLogId() : UUID.randomUUID().toString();
        in.setLogId(logId);

        // Build human-readable message once
        final String pretty = buildMessage(
                in.getService(), in.getStage(), in.getMessageId(), in.getStatus(), in.getErrors());
        in.setMessage(pretty);

        try {
            AuditLogs saved = repo.save(in);
            log.info("audit.persisted service={} stage={} status={} messageId={} logId={} mongoId={}",
                    nz(in.getService()), nz(in.getStage()), nz(in.getStatus()),
                    nz(in.getMessageId()), logId, saved.getId());
        } catch (Exception e) {
            log.error("audit.mongo_failed logId={} reason={}", logId, e.toString(), e);
        }
    }

    /** Convenience overload for simple call sites. */
    public void log(String service,
                    String stage,
                    String messageId,
                    String status,
                    Map<String, Object> details) {

        AuditLogs logDoc = AuditLogs.builder()
                .service(service)
                .stage(stage)
                .messageId(messageId)
                .status(status)
                .details(details)
                .build();

        log(logDoc);
    }

    // --- Optional convenience helpers specific to normalizer ---

    public void success(String messageId, Map<String, Object> details) {
        log("normalization-service", "Normalization", messageId, "SUCCESS", details);
    }

    public void failure(String messageId, String error, Map<String, Object> details) {
        log(AuditLogs.builder()
                .service("normalization-service")
                .stage("Normalization")
                .messageId(messageId)
                .status("FAILED")
                .errors(List.of(error))
                .details(details)
                .build());
    }

    public void duplicate(String messageId, Map<String, Object> details) {
        log("normalization-service", "Normalization", messageId, "DROPPED_DUPLICATE", details);
    }

    public void kafkaSent(String messageId, String topic) {
        log("normalization-service", "Normalization", messageId, "KAFKA_SENT", Map.of("topic", topic));
    }

    public void kafkaFailed(String messageId, String topic, String error) {
        failure(messageId, error, Map.of("topic", topic));
    }

    // --- internals ---

    private static String buildMessage(String service,
                                       String stage,
                                       String messageId,
                                       String status,
                                       List<String> errors) {
        String base = String.format(
                "[%s] service=%s | stage=%s | messageId=%s | status=%s",
                Instant.now(),
                nz(service), nz(stage), nz(messageId), nz(status)
        );
        if (errors != null && !errors.isEmpty()) {
            return base + " | errors=" + errors;
        }
        return base;
    }

    private static String nz(String s) { return (s == null) ? "-" : s; }
}
