package com.pm.team1_dash_v.controller;

import com.pm.team1_dash_v.service.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {
    private final MessageService messageService;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessageController.class);

    @PostMapping("/email")
    public ResponseEntity<?> ingestEmail(@RequestBody JsonNode body) {
        try {
            log.info("Incoming email payload:\n{}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
        } catch (Exception ignore) {
            log.info("Incoming email payload: {}", body.toString());
        }

        var resp = messageService.ingestEmail(body);
        return ResponseEntity.accepted().body(resp);
    }

    @PostMapping("/slack")
    public ResponseEntity<?> ingestSlack(@RequestBody JsonNode body) {
        try {
            log.info("Incoming slack payload:\n{}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
        } catch (Exception ignore) {
            log.info("Incoming slack payload: {}", body.toString());
        }

        var resp = messageService.ingestSlack(body);
        return ResponseEntity.accepted().body(resp);
    }
}
