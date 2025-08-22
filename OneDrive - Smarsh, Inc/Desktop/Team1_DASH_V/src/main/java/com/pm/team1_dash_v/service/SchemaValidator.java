package com.pm.team1_dash_v.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
public class SchemaValidator {

    private static final JsonSchemaFactory FACTORY =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ConcurrentMap<String, JsonSchema> CACHE = new ConcurrentHashMap<>();

    /** Validate using a classpath path like "schemas/email.schema.json". */
    public void validate(String schemaClasspathPath, JsonNode instance) {
        if (schemaClasspathPath == null || schemaClasspathPath.isBlank()) {
            throw new IllegalArgumentException("schemaClasspathPath is blank. Example: schemas/email.schema.json");
        }
        JsonSchema schema = CACHE.computeIfAbsent(schemaClasspathPath, this::loadSchemaOrThrow);
        Set<ValidationMessage> errors = schema.validate(instance);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(formatErrors(errors));
        }
    }

    /** Overload: pass raw JSON string to validate. */
    public void validate(String schemaClasspathPath, String json) {
        try {
            validate(schemaClasspathPath, MAPPER.readTree(json));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON instance: " + e.getMessage(), e);
        }
    }

    private JsonSchema loadSchemaOrThrow(String path) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return FACTORY.getSchema(in);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(
                    "Schema not found on classpath: " + path +
                            " (place it under src/main/resources, e.g., src/main/resources/" + path + ")", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load schema from classpath: " + path, e);
        }
    }

    private static String formatErrors(Set<ValidationMessage> errors) {
        return errors.stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.joining("; "));
    }
}
