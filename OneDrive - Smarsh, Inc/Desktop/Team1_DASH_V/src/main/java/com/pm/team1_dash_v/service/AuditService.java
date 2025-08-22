package com.pm.team1_dash_v.service;

import com.pm.team1_dash_v.model.AuditLogs;
import com.pm.team1_dash_v.repository.AuditLogRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

//@Service
//@RequiredArgsConstructor
//public class AuditService {
//    private final ElasticsearchClient es;
////@Bean
//    public void log(AuditDoc d) {
//        try {
//            es.index(i -> i.index("audit")
//                    .id(d.getLogId())
//                    .document(Map.of(
//                            "@timestamp", d.getTimestamp().toString(),
//                            "logId", d.getLogId(),
//                            "stage", d.getStage(),
//                            "service", d.getService(),
//                            "messageId", d.getMessageId(),
//                            "status", d.getStatus(),
//                            "errors", d.getErrors(),
//                            "rulesChecked", d.getRulesChecked(),
//                            "matchedRules", d.getMatchedRules(),
//                            "details", d.getDetails()
//                    )));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}

@Service
@RequiredArgsConstructor
public class AuditService {
    private final ElasticsearchClient es;
    private final AuditLogRepository repo;

    public void log(AuditLogs d) {
        AuditLogs m = AuditLogs.builder().build();
        m.setLogId(d.getLogId());
        m.setStage(d.getStage());
        m.setService(d.getService());
        m.setMessageId(d.getMessageId());
        m.setStatus(d.getStatus());
        m.setErrors(d.getErrors());
        m.setRulesChecked(d.getRulesChecked());
        m.setMatchedRules(d.getMatchedRules());
        m.setDetails(d.getDetails()); // createdAt is set automatically via @CreatedDate if auditing is enabled
        repo.save(m);
        try {
            es.index(i -> i.index("audit")
                    .id(d.getLogId())
                    .document(d)); // index the POJO directly
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}