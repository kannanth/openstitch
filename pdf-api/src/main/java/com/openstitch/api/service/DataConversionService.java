package com.openstitch.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstitch.engine.exception.DataParseException;
import com.openstitch.engine.parser.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * Service responsible for converting incoming data (JSON, XML, CSV) into a DataContext
 * that the PDF engine can use for template binding.
 */
@Service
public class DataConversionService {

    private final JsonDataParser jsonParser = new JsonDataParser();
    private final XmlDataParser xmlParser = new XmlDataParser();
    private final CsvDataParser csvParser = new CsvDataParser();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse data from the request into a DataContext based on the specified format.
     *
     * @param data       the data object (used for JSON format)
     * @param dataString the raw data string (used for XML/CSV formats, also accepted for JSON)
     * @param dataFormat the format: "JSON", "XML", or "CSV" (defaults to "JSON" if null)
     * @return a DataContext containing the parsed data
     * @throws DataParseException if parsing fails
     */
    public DataContext parseData(Object data, String dataString, String dataFormat) {
        if (dataFormat == null) dataFormat = "JSON";

        Map<String, Object> dataMap;
        switch (dataFormat.toUpperCase()) {
            case "XML":
                if (dataString == null || dataString.isBlank()) {
                    throw new DataParseException("XML data must be provided as dataString");
                }
                dataMap = xmlParser.parse(dataString);
                break;
            case "CSV":
                if (dataString == null || dataString.isBlank()) {
                    throw new DataParseException("CSV data must be provided as dataString");
                }
                dataMap = csvParser.parse(dataString);
                break;
            case "JSON":
            default:
                if (data != null) {
                    try {
                        String json = objectMapper.writeValueAsString(data);
                        dataMap = jsonParser.parse(json);
                    } catch (Exception e) {
                        throw new DataParseException("Failed to parse JSON data: " + e.getMessage(), e);
                    }
                } else if (dataString != null && !dataString.isBlank()) {
                    dataMap = jsonParser.parse(dataString);
                } else {
                    dataMap = Collections.emptyMap();
                }
                break;
        }

        return new DataContext(dataMap);
    }
}
