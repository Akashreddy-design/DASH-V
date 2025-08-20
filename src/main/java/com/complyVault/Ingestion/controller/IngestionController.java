package com.complyVault.Ingestion.controller;

import com.complyVault.Ingestion.model.EmailMessageRaw;
import com.complyVault.Ingestion.model.SlackMessageRaw;
import com.complyVault.Ingestion.service.IngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ingest")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/email")
    public ResponseEntity<String> ingestEmail(@RequestBody EmailMessageRaw email) {
        String messageId = ingestionService.ingestEmail(email);
        return ResponseEntity.ok("Ingested Email with ID: " + messageId);
    }

    @PostMapping("/slack")
    public ResponseEntity<String> ingestSlack(@RequestBody SlackMessageRaw slack) {
        String messageId = ingestionService.ingestSlack(slack);
        return ResponseEntity.ok("Ingested Slack with ID: " + messageId);
    }
}
