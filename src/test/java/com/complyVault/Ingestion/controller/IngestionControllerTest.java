// src/test/java/com/complyVault/Ingestion/controller/IngestionControllerTest.java
package com.complyVault.Ingestion.controller;

import com.complyVault.Ingestion.model.EmailMessageRaw;
import com.complyVault.Ingestion.model.SlackMessageRaw;
import com.complyVault.Ingestion.service.IngestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for IngestionController.
 * This class uses Mockito to test the controller layer in isolation
 * by manually setting up MockMvc with a mock service.
 */
@ExtendWith(MockitoExtension.class)
class IngestionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private IngestionService ingestionService;

    @InjectMocks
    private IngestionController ingestionController;

    // Define a simple ControllerAdvice for the test to handle the specific exception.
    @ControllerAdvice
    private static class TestExceptionHandler {
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @BeforeEach
    void setUp() {
        // Manually set up MockMvc with the controller instance and the test exception handler.
        // Also configure the ObjectMapper to support Java 8 Date/Time types.
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        this.mockMvc = MockMvcBuilders.standaloneSetup(ingestionController)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should successfully ingest an email and return a message ID")
    void ingestEmail_Success() throws Exception {
        // Arrange
        // Create a sample EmailMessageRaw object to send in the request body.
        EmailMessageRaw email = new EmailMessageRaw();
        email.setTenantId("test-tenant");
        email.setNetwork("email");
        EmailMessageRaw.EmailPayload payload = new EmailMessageRaw.EmailPayload();
        payload.setFrom("test@example.com");
        payload.setTo(Collections.singletonList("recipient@example.com"));
        payload.setSubject("Test Subject");
        payload.setBody("Test Body");
        payload.setSentAt(Instant.now());
        email.setPayload(payload);

        // Define the expected message ID from the service mock.
        String expectedMessageId = UUID.nameUUIDFromBytes("some-unique-value".getBytes()).toString();

        // Configure the mock service to return the expected message ID
        // when the ingestEmail method is called with any EmailMessageRaw object.
        when(ingestionService.ingestEmail(any(EmailMessageRaw.class))).thenReturn(expectedMessageId);

        // Act & Assert
        // Perform a POST request to the /ingest/email endpoint with the email JSON.
        // Expect a 200 OK status and the correct response body.
        mockMvc.perform(post("/ingest/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(email)))
                .andExpect(status().isOk())
                .andExpect(content().string("Ingested Email with ID: " + expectedMessageId));
    }

    @Test
    @DisplayName("Should successfully ingest a slack message and return a message ID")
    void ingestSlack_Success() throws Exception {
        // Arrange
        // Create a sample SlackMessageRaw object.
        SlackMessageRaw slack = new SlackMessageRaw();
        slack.setTenantId("test-tenant");
        slack.setNetwork("slack");
        slack.setUser("test-user");
        slack.setText("Hello, World!");
        slack.setTimestamp(Instant.now());
        slack.setTeam("test-team");
        slack.setChannel("test-channel");

        String expectedMessageId = UUID.nameUUIDFromBytes("some-other-unique-value".getBytes()).toString();

        // Configure the mock service to return the expected message ID.
        when(ingestionService.ingestSlack(any(SlackMessageRaw.class))).thenReturn(expectedMessageId);

        // Act & Assert
        // Perform a POST request to the /ingest/slack endpoint.
        mockMvc.perform(post("/ingest/slack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(slack)))
                .andExpect(status().isOk())
                .andExpect(content().string("Ingested Slack with ID: " + expectedMessageId));
    }

    @Test
    @DisplayName("Should return Bad Request for invalid email payload (missing tenantId)")
    void ingestEmail_InvalidPayload_BadRequest() throws Exception {
        // Arrange
        EmailMessageRaw email = new EmailMessageRaw(); // missing tenantId
        email.setNetwork("email");
        EmailMessageRaw.EmailPayload payload = new EmailMessageRaw.EmailPayload();
        payload.setFrom("test@example.com");
        payload.setSentAt(Instant.now());
        email.setPayload(payload);

        // Configure the mock to throw an exception when validation fails.
        when(ingestionService.ingestEmail(any(EmailMessageRaw.class)))
                .thenThrow(new IllegalArgumentException("Invalid Email JSON: missing tenantId"));

        // Act & Assert
        // Send a request with the invalid payload. We expect a 4xx status.
        mockMvc.perform(post("/ingest/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(email)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return Bad Request for invalid slack payload (missing user)")
    void ingestSlack_InvalidPayload_BadRequest() throws Exception {
        // Arrange
        SlackMessageRaw slack = new SlackMessageRaw(); // missing user
        slack.setTenantId("test-tenant");
        slack.setTimestamp(Instant.now());

        when(ingestionService.ingestSlack(any(SlackMessageRaw.class)))
                .thenThrow(new IllegalArgumentException("Invalid Slack JSON: missing user"));

        // Act & Assert
        mockMvc.perform(post("/ingest/slack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(slack)))
                .andExpect(status().isBadRequest());
    }
}
