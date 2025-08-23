package com.example.demo.controller;

import com.example.demo.model.NormalizedMessage;
import com.example.demo.search.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final SearchService searchService;

    public MessageController(SearchService searchService) {
        this.searchService = searchService;
    }

    // ✅ Insert single message
    @PostMapping
    public ResponseEntity<String> addMessage(@RequestBody NormalizedMessage message) throws IOException {
        searchService.indexMessage(message);
        return ResponseEntity.ok("Message indexed");
    }

    // ✅ Bulk insert
    @PostMapping("/bulk")
    public ResponseEntity<String> addMessages(@RequestBody List<NormalizedMessage> messages) throws IOException {
        for (NormalizedMessage message : messages) {
            searchService.indexMessage(message);
        }
        return ResponseEntity.ok(messages.size() + " messages indexed");
    }


    // ✅ Full-text search
    @GetMapping("/search")
    public ResponseEntity<List<NormalizedMessage>> search(@RequestParam String query) throws IOException {
        return ResponseEntity.ok(searchService.search(query)); // ✅ pass it along
    }

    // ✅ Keyword search
    @GetMapping("/search/keyword")
    public ResponseEntity<List<NormalizedMessage>> searchByKeyword(@RequestParam String field,
                                                                   @RequestParam String value) throws IOException {
        return ResponseEntity.ok(searchService.searchByKeyword(field, value));
    }
}
