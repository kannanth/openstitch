package com.openstitch.engine.parser;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.MappingIterator;
import com.openstitch.engine.exception.DataParseException;
import java.util.*;

/**
 * Parses CSV data strings into a Map with a "rows" key containing a list of maps (one per row).
 * Expects the first line to be a header row.
 */
public class CsvDataParser implements DataParser {
    private final CsvMapper csvMapper;

    public CsvDataParser() {
        this.csvMapper = new CsvMapper();
    }

    @Override
    public Map<String, Object> parse(String data) throws DataParseException {
        try {
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            MappingIterator<Map<String, String>> iterator = csvMapper
                .readerFor(Map.class)
                .with(schema)
                .readValues(data);

            List<Map<String, Object>> rows = new ArrayList<>();
            while (iterator.hasNext()) {
                Map<String, String> row = iterator.next();
                rows.add(new LinkedHashMap<>(row));
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("rows", rows);
            return result;
        } catch (Exception e) {
            throw new DataParseException("Failed to parse CSV data: " + e.getMessage(), e);
        }
    }
}
