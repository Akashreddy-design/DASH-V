package com.complyvault.policy_engine.service;

import com.complyvault.policy_engine.model.AuditLogs;
import com.complyvault.policy_engine.db.AuditLogRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
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

    private final ElasticsearchClient es;
    private final AuditLogRepository repo;

    /**
     * Persist audit to Mongo and index in Elasticsearch.
     * - Guarantees logId
     * - Builds a readable message
     * - Mongo write is primary; ES index is best-effort
     */
    public void log(AuditLogs in) {
        // Ensure a correlation id
        final String logId = in.getLogId() != null ? in.getLogId() : UUID.randomUUID().toString();
        in.setLogId(logId);

        // Build human-readable line once
        in.setMessage(buildMessage(in.getService(), in.getStage(), in.getMessageId(), in.getStatus(), in.getErrors()));

        // 1) Mongo (source of truth)
        try {
            AuditLogs saved = repo.save(in);
            log.info("audit.persisted service={} stage={} status={} messageId={} logId={} mongoId={}",
                    nz(in.getService()), nz(in.getStage()), nz(in.getStatus()),
                    nz(in.getMessageId()), logId, saved.getId());
        } catch (Exception e) {
            log.error("audit.mongo_failed logId={} reason={}", logId, e.toString(), e);
            // If Mongo fails, we still attempt ES for visibility, but we rethrow afterwards.
            tryIndexEs(in, logId);
            throw e instanceof RuntimeException re ? re : new RuntimeException(e);
        }

        // 2) Elasticsearch (best-effort)
        tryIndexEs(in, logId);
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

    // --- Optional helpers specific to policy engine ---

    public void evaluationStarted(String messageId, Map<String, Object> details) {
        log("policy-engine", "Evaluation", messageId, "STARTED", details);
    }

    public void evaluationSucceeded(String messageId, List<String> matchedRules, Map<String, Object> details) {
        AuditLogs doc = AuditLogs.builder()
                .service("policy-engine")
                .stage("Evaluation")
                .messageId(messageId)
                .status("SUCCESS")
                .matchedRules(matchedRules)
                .details(details)
                .build();
        log(doc);
    }

    public void evaluationFailed(String messageId, String error, Map<String, Object> details) {
        AuditLogs doc = AuditLogs.builder()
                .service("policy-engine")
                .stage("Evaluation")
                .messageId(messageId)
                .status("FAILED")
                .errors(List.of(error))
                .details(details)
                .build();
        log(doc);
    }

    // --- internals ---

    private void tryIndexEs(AuditLogs in, String logId) {
        try {
            es.index(i -> i.index("audit").id(logId).document(in));
            log.info("audit.es_indexed index=audit id={}", logId);
        } catch (Exception esEx) {
            log.error("audit.es_failed id={} reason={}", logId, esEx.toString(), esEx);
        }
    }

    private static String buildMessage(String service,
                                       String stage,
                                       String messageId,
                                       String status,
                                       List<String> errors) {
        String base = String.format("[%s] service=%s | stage=%s | messageId=%s | status=%s",
                Instant.now(), nz(service), nz(stage), nz(messageId), nz(status));
        if (errors != null && !errors.isEmpty()) {
            return base + " | errors=" + errors;
        }
        return base;
    }

    private static String nz(String s) { return s == null ? "-" : s; }
}
