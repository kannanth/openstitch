package com.openstitch.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstitch.engine.OpenStitchEngine;
import com.openstitch.engine.exception.TemplateParseException;
import com.openstitch.engine.model.Template;
import com.openstitch.engine.parser.DataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    private final OpenStitchEngine engine;
    private final ObjectMapper objectMapper;
    private final DataConversionService dataConversionService;

    public PdfGenerationService(DataConversionService dataConversionService) {
        this.engine = new OpenStitchEngine();
        this.objectMapper = new ObjectMapper();
        this.dataConversionService = dataConversionService;
    }

    /**
     * Generate a PDF from a template object and a data object (JSON format).
     * Delegates to the multi-format overload with JSON defaults.
     *
     * @param templateObj the template definition as a deserialized JSON object
     * @param dataObj     the data to bind into the template (may be null)
     * @return the generated PDF as a byte array
     */
    public byte[] generatePdf(Object templateObj, Object dataObj) {
        return generatePdf(templateObj, dataObj, null, null);
    }

    /**
     * Generate a PDF from a template object with support for multiple data formats.
     *
     * @param templateObj the template definition as a deserialized JSON object
     * @param data        the data object (used for JSON format, may be null)
     * @param dataString  the raw data string (used for XML/CSV formats, may be null)
     * @param dataFormat  the data format: "JSON", "XML", or "CSV" (defaults to "JSON")
     * @return the generated PDF as a byte array
     */
    public byte[] generatePdf(Object templateObj, Object data, String dataString, String dataFormat) {
        try {
            String templateJson = objectMapper.writeValueAsString(templateObj);
            Template template = engine.parseTemplate(templateJson);

            DataContext dataContext = dataConversionService.parseData(data, dataString, dataFormat);

            log.debug("Generating PDF with template and data (format: {})", dataFormat != null ? dataFormat : "JSON");
            return engine.generatePdf(template, dataContext);
        } catch (JsonProcessingException e) {
            throw new TemplateParseException("Failed to serialize request objects to JSON: " + e.getMessage(), e);
        }
    }
}
