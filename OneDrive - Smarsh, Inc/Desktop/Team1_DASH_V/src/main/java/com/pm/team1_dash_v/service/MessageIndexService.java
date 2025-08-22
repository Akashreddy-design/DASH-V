package com.pm.team1_dash_v.service;

import com.pm.team1_dash_v.model.MessageDoc;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Map;

@Service @RequiredArgsConstructor
public class MessageIndexService {
    private final ElasticsearchClient es;

    public void index(MessageDoc d) {
        String id = d.getDedupeHash() != null ? d.getDedupeHash() : d.getMessageId();
        try {
            es.index(i -> i.index("msgs").id(id).document(Map.of(
                    "@timestamp", d.getTimestamp().toString(),
                    "messageId", d.getMessageId(),
                    "source", d.getSource(),
                    "channel", d.getChannel(),
                    "userId", d.getUserId(),
                    "dedupeHash", d.getDedupeHash(),
                    "status", d.getStatus(),
                    "text", d.getText(),
                    "metadata", d.getMetadata()
            )));
        } catch (IOException e) { throw new RuntimeException(e); }
    }
}
