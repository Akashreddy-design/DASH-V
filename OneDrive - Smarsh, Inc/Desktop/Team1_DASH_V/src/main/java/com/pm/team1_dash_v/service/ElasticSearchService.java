package com.pm.team1_dash_v.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.pm.team1_dash_v.model.MessageDoc;
import com.pm.team1_dash_v.model.AuditLogs;

@Service
@RequiredArgsConstructor
public class ElasticSearchService {
    private final ElasticsearchClient es;

    public void indexMessage(MessageDoc doc) throws IOException {
        es.index(i -> i.index("msgs")
                .id(doc.getDedupeHash() != null ? doc.getDedupeHash() : doc.getMessageId())
                .document(Map.of(
                        "@timestamp", doc.getTimestamp().toString(),
                        "messageId",  doc.getMessageId(),
                        "source",     doc.getSource(),
                        "channel",    doc.getChannel(),
                        "userId",     doc.getUserId(),
                        "dedupeHash", doc.getDedupeHash(),
                        "status",     doc.getStatus(),
                        "text",       doc.getText(),
                        "metadata",   doc.getMetadata()
                )));
    }

    public void indexAudit(AuditLogs doc) throws IOException {
        es.index(i -> i.index("audit")
                .id(doc.getLogId())
                .document(Map.of(
                        "@timestamp", doc.getTimestamp().toString(),
                        "logId",      doc.getLogId(),
                        "stage",      doc.getStage(),
                        "service",    doc.getService(),
                        "messageId",  doc.getMessageId(),
                        "status",     doc.getStatus(),
                        "errors",     doc.getErrors(),
                        "rulesChecked", doc.getRulesChecked(),
                        "matchedRules", doc.getMatchedRules(),
                        "details",    doc.getDetails()
                )));
    }

    public List<Map> searchMessages(String queryText) throws IOException {
        var res = es.search(s -> s.index("msgs")
                .query(q -> q.multiMatch(mm -> mm.query(queryText).fields("text"))), Map.class);
        return res.hits().hits().stream().map(h -> h.source()).toList();
    }
}