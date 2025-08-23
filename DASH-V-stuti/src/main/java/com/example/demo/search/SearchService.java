package com.example.demo.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.demo.model.NormalizedMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class SearchService {

    private final ElasticsearchClient es;

    public SearchService(ElasticsearchClient es) {
        this.es = es;
    }

    // Index a POJO
    public void indexMessage(NormalizedMessage message) throws IOException {
        IndexResponse resp = es.index(i -> i
                .index("messages")
                .id(message.getMessageId())
                .document(message)
        );
        System.out.println("Indexed version: " + resp.version());
    }

    // Full-text search - corrected field names
    public List<NormalizedMessage> search(String q) throws IOException {
        SearchRequest req = SearchRequest.of(s -> s
                .index("messages")
                .query(qb -> qb.multiMatch(mm -> mm
                        .query(q)
                        // Fixed field names to match your actual data structure
                        .fields("body^4", "subject^3", "sender^2", "team")
                        .type(TextQueryType.BestFields)
                        .operator(Operator.Or)  // Changed to Or for better results
                ))
                // Remove timestamp sort since it might be null
                .size(50)
        );

        SearchResponse<NormalizedMessage> res = es.search(req, NormalizedMessage.class);
        List<NormalizedMessage> out = new ArrayList<>();
        for (Hit<NormalizedMessage> h : res.hits().hits()) {
            if (h.source() != null) out.add(h.source());
        }
        return out;
    }

    // Exact/keyword search - corrected field handling
    public List<NormalizedMessage> searchByKeyword(String field, String value) throws IOException {
        // Handle field mapping based on your actual data structure
        String queryField = switch (field) {
            case "sender", "body", "subject", "team" -> field + ".keyword";
            case "network", "tenantId", "messageId", "id" -> field;  // These are usually already keyword fields
            default -> field;
        };

        SearchResponse<NormalizedMessage> res = es.search(s -> s
                        .index("messages")
                        .query(q -> q.term(t -> t.field(queryField).value(value)))
                        .size(50)
                , NormalizedMessage.class);

        List<NormalizedMessage> out = new ArrayList<>();
        for (Hit<NormalizedMessage> h : res.hits().hits()) {
            if (h.source() != null) out.add(h.source());
        }
        return out;
    }
}

