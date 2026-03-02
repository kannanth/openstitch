package com.openstitch.engine.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstitch.engine.exception.DataParseException;

import java.util.Map;

/**
 * Parses JSON data strings into maps using Jackson ObjectMapper.
 * Handles nested objects and arrays naturally through Jackson's default
 * deserialization behavior (nested objects become nested Maps, arrays become Lists).
 */
public class JsonDataParser implements DataParser {

    private final ObjectMapper objectMapper;

    public JsonDataParser() {
        this.objectMapper = new ObjectMapper();
    }

    public JsonDataParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> parse(String data) throws DataParseException {
        if (data == null || data.isBlank()) {
            throw new DataParseException("Data string must not be null or blank");
        }

        try {
            return objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new DataParseException("Failed to parse JSON data: " + e.getMessage(), e);
        }
    }
}
