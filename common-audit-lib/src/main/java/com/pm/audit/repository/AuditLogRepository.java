package com.pm.audit.repository;

import com.pm.audit.model.AuditLogs;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLogs, String> {
}
