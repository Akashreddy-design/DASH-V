package com.pm.audit.service;

import com.pm.audit.model.AuditLogs;
import com.pm.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLogs save(AuditLogs log) {
        return auditLogRepository.save(log);
    }

    public List<AuditLogs> getAllLogs() {
        return auditLogRepository.findAll();
    }
}
