package com.dharani.ingestion.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.Set;

public final class JsonSchemaValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonSchemaValidator() { }

    public static void validate(String rawJson, String messageType) {
        if (rawJson == null || rawJson.isBlank())
            throw new IllegalArgumentException("Payload is empty.");
        if (messageType == null || messageType.isBlank())
            throw new IllegalArgumentException("messageType is required.");

        // 1) Parse JSON
        final JsonNode json;
        try {
            json = MAPPER.readTree(rawJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage());
        }

        // Optional: enforce object at root
        // if (!json.isObject()) throw new IllegalArgumentException("Root must be a JSON object.");

        // 2) Load schema
        String type = messageType.trim().toLowerCase();
        String schemaPath = "schemas/" + type + "-schema.json";

        try (InputStream in = new ClassPathResource(schemaPath).getInputStream()) {
            JsonSchema schema = JsonSchemaFactory
                    .getInstance(SpecVersion.VersionFlag.V7)
                    .getSchema(in);

            // 3) Validate
            Set<ValidationMessage> errors = schema.validate(json);
            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("Invalid " + type + " message: " + errors);
            }
        } catch (java.io.IOException notFound) {
            throw new IllegalArgumentException("Schema file not found: " + schemaPath);
        } catch (Exception e) {
            throw new IllegalArgumentException("Schema validation error: " + e.getMessage());
        }
    }
}
