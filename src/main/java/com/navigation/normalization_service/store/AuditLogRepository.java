package com.navigation.normalization_service.store;

import com.navigation.normalization_service.model.AuditLogs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLogs, String> {
    // Add projections/queries later if needed (e.g., findTop50ByMessageIdOrderByCreatedAtDesc)
}
