package com.dharani.ingestion.controller;

import com.dharani.ingestion.service.MessageProcessorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ingest")
public class IngestionController {

    private final MessageProcessorService messageProcessorService;

    public IngestionController(MessageProcessorService messageProcessorService) {
        this.messageProcessorService = messageProcessorService;
    }

    @PostMapping("/email")
    public ResponseEntity<String> ingestEmail(@RequestBody String rawEmailJson) {
        try {
            messageProcessorService.processMessage(rawEmailJson, "email");
            return new ResponseEntity<>("Email payload accepted for processing.", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/slack")
    public ResponseEntity<String> ingestSlack(@RequestBody String rawSlackJson) {
        try {
            messageProcessorService.processMessage(rawSlackJson, "slack");
            return new ResponseEntity<>("Slack payload accepted for processing.", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}