package com.openstitch.engine.parser;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.openstitch.engine.exception.DataParseException;
import java.util.Map;

/**
 * Parses XML data strings into maps using Jackson XmlMapper.
 */
public class XmlDataParser implements DataParser {
    private final XmlMapper xmlMapper;

    public XmlDataParser() {
        this.xmlMapper = new XmlMapper();
    }

    @Override
    public Map<String, Object> parse(String data) throws DataParseException {
        try {
            return xmlMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new DataParseException("Failed to parse XML data: " + e.getMessage(), e);
        }
    }
}
