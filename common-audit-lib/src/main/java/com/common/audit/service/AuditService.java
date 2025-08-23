package com.common.audit.service;

import com.common.audit.model.AuditLogs;
import com.common.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repo;
    private final PayloadSanitizer sanitizer;

    public AuditLogs save(AuditLogs log) {
        if (log.getRequestPayload() != null) {
            // truncate very large payloads (e.g., 64KB); adjust as needed
            String payload = log.getRequestPayload();
            int limit = 64 * 1024;
            if (payload.length() > limit) payload = payload.substring(0, limit);
            log.setRequestPayload(sanitizer.sanitize(payload));
        }
        return repo.save(log);
    }
}
