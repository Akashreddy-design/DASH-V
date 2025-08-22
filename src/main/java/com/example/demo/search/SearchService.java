package com.example.demo.search;

import com.example.demo.model.NormalizedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final RestHighLevelClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public SearchService(RestHighLevelClient client) {
        this.client = client;
    }

    // ✅ Index a single message
    public void indexMessage(NormalizedMessage message) throws IOException {
        Map<String, Object> dataMap = mapper.convertValue(message, Map.class);
        IndexRequest request = new IndexRequest("messages")
                .id(message.getMessageId())
                .source(dataMap);
        client.index(request, RequestOptions.DEFAULT);
    }

    // ✅ Full-text search (returns only messages)
    public List<NormalizedMessage> search(String query) throws IOException {
        SearchRequest searchRequest = new SearchRequest("messages");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchQuery("content", query));
        searchRequest.source(builder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        List<NormalizedMessage> results = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            NormalizedMessage msg = mapper.convertValue(hit.getSourceAsMap(), NormalizedMessage.class);
            results.add(msg);
        }
        return results;
    }

    // ✅ Keyword search (returns only messages)
    public List<NormalizedMessage> searchByKeyword(String field, String value) throws IOException {
        SearchRequest searchRequest = new SearchRequest("messages");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.termQuery(field, value));
        searchRequest.source(builder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        List<NormalizedMessage> results = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            NormalizedMessage msg = mapper.convertValue(hit.getSourceAsMap(), NormalizedMessage.class);
            results.add(msg);
        }
        return results;
    }
}
