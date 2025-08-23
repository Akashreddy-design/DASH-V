package com.common.audit.repository;

import com.common.audit.model.AuditLogs;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLogs, String> {}
