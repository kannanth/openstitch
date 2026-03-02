package com.openstitch.engine.parser;

import com.openstitch.engine.exception.DataParseException;
import java.util.Map;

/**
 * Interface for parsing data strings into key-value maps.
 * Implementations handle specific formats (JSON, XML, CSV, etc.).
 */
public interface DataParser {

    /**
     * Parse a data string into a map of key-value pairs.
     *
     * @param data the data string to parse
     * @return a map of parsed data
     * @throws DataParseException if the data cannot be parsed
     */
    Map<String, Object> parse(String data) throws DataParseException;
}
