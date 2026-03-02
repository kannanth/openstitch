package com.openstitch.engine.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstitch.engine.exception.TemplateParseException;
import com.openstitch.engine.model.Template;

/**
 * Parses a template JSON string into a {@link Template} model object
 * and serializes Template objects back to JSON.
 */
public class TemplateParser {

    private final ObjectMapper objectMapper;

    public TemplateParser() {
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Parse a JSON string into a Template model.
     *
     * @param json the JSON template string
     * @return the parsed Template
     * @throws TemplateParseException if parsing fails
     */
    public Template parse(String json) throws TemplateParseException {
        if (json == null || json.isBlank()) {
            throw new TemplateParseException("Template JSON must not be null or blank");
        }

        try {
            return objectMapper.readValue(json, Template.class);
        } catch (JsonProcessingException e) {
            throw new TemplateParseException("Failed to parse template JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Serialize a Template model to a JSON string.
     *
     * @param template the Template to serialize
     * @return the JSON string representation
     * @throws TemplateParseException if serialization fails
     */
    public String serialize(Template template) throws TemplateParseException {
        if (template == null) {
            throw new TemplateParseException("Template must not be null");
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(template);
        } catch (JsonProcessingException e) {
            throw new TemplateParseException("Failed to serialize template: " + e.getMessage(), e);
        }
    }
}
