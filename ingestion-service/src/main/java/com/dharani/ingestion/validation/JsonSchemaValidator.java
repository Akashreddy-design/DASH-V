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

public class JsonSchemaValidator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void validate(String rawJson, String messageType) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(rawJson);

        String schemaFile = "schemas/" + messageType + "-schema.json";
        InputStream schemaStream = new ClassPathResource(schemaFile).getInputStream();

        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
                .getSchema(schemaStream);

        Set<ValidationMessage> errors = schema.validate(jsonNode);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid " + messageType + " message: " + errors);
        }
    }
}
