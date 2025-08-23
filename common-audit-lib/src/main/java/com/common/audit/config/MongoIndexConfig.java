package com.common.audit.config;

import com.common.audit.model.AuditLogs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
@RequiredArgsConstructor
public class MongoIndexConfig {
    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void ensureIndexes() {
        mongoTemplate.indexOps(AuditLogs.class)
                .ensureIndex(new Index().on("timestamp", Sort.Direction.DESC));
        mongoTemplate.indexOps(AuditLogs.class)
                .ensureIndex(new Index().on("serviceName", Sort.Direction.ASC));
        mongoTemplate.indexOps(AuditLogs.class)
                .ensureIndex(new Index().on("action", Sort.Direction.ASC));
        mongoTemplate.indexOps(AuditLogs.class)
                .ensureIndex(new Index().on("traceId", Sort.Direction.ASC));
    }
}
