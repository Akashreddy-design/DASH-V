package com.complyvault.policy_engine.db;

import com.complyvault.policy_engine.model.AuditLogs;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLogs, String> {}
