package com.openstitch.engine;

import com.openstitch.engine.exception.DataParseException;
import com.openstitch.engine.exception.RenderException;
import com.openstitch.engine.exception.TemplateParseException;
import com.openstitch.engine.model.Template;
import com.openstitch.engine.parser.CsvDataParser;
import com.openstitch.engine.parser.DataContext;
import com.openstitch.engine.parser.JsonDataParser;
import com.openstitch.engine.parser.TemplateParser;
import com.openstitch.engine.parser.XmlDataParser;
import com.openstitch.engine.render.PdfRenderer;

import java.util.Collections;
import java.util.Map;

/**
 * Main entry point facade for the OpenStitch PDF engine.
 * Provides a simplified API for parsing templates, processing data, and generating PDFs.
 */
public class OpenStitchEngine {

    private final TemplateParser templateParser;
    private final JsonDataParser jsonDataParser;
    private final PdfRenderer pdfRenderer;

    public OpenStitchEngine() {
        this.templateParser = new TemplateParser();
        this.jsonDataParser = new JsonDataParser();
        this.pdfRenderer = new PdfRenderer();
    }

    /**
     * Parse a template JSON string and generate a PDF with the given JSON data.
     *
     * @param templateJson the template definition as a JSON string
     * @param dataJson     the data to bind into the template as a JSON string (may be null for no data)
     * @return the generated PDF as a byte array
     * @throws TemplateParseException if the template JSON is invalid
     * @throws DataParseException     if the data JSON is invalid
     * @throws RenderException        if PDF rendering fails
     */
    public byte[] generatePdf(String templateJson, String dataJson) {
        Template template = parseTemplate(templateJson);

        DataContext dataContext;
        if (dataJson != null && !dataJson.isBlank()) {
            Map<String, Object> data = jsonDataParser.parse(dataJson);
            dataContext = new DataContext(data);
        } else {
            dataContext = new DataContext(Collections.emptyMap());
        }

        return generatePdf(template, dataContext);
    }

    /**
     * Generate a PDF from a parsed Template and a DataContext.
     *
     * @param template    the parsed template
     * @param dataContext the data context for expression resolution
     * @return the generated PDF as a byte array
     * @throws RenderException if PDF rendering fails
     */
    public byte[] generatePdf(Template template, DataContext dataContext) {
        return pdfRenderer.render(template, dataContext);
    }

    /**
     * Parse a template JSON string into a Template model object.
     *
     * @param templateJson the template JSON string
     * @return the parsed Template
     * @throws TemplateParseException if parsing fails
     */
    public Template parseTemplate(String templateJson) {
        return templateParser.parse(templateJson);
    }

    /**
     * Parse a template JSON string and generate a PDF with XML data.
     *
     * @param templateJson the template definition as a JSON string
     * @param xmlData      the data as an XML string
     * @return the generated PDF as a byte array
     * @throws TemplateParseException if the template JSON is invalid
     * @throws DataParseException     if the XML data is invalid
     * @throws RenderException        if PDF rendering fails
     */
    public byte[] generatePdfFromXml(String templateJson, String xmlData) {
        Template template = parseTemplate(templateJson);
        XmlDataParser xmlParser = new XmlDataParser();
        Map<String, Object> data = xmlParser.parse(xmlData);
        return generatePdf(template, new DataContext(data));
    }

    /**
     * Parse a template JSON string and generate a PDF with CSV data.
     *
     * @param templateJson the template definition as a JSON string
     * @param csvData      the data as a CSV string (first line is header)
     * @return the generated PDF as a byte array
     * @throws TemplateParseException if the template JSON is invalid
     * @throws DataParseException     if the CSV data is invalid
     * @throws RenderException        if PDF rendering fails
     */
    public byte[] generatePdfFromCsv(String templateJson, String csvData) {
        Template template = parseTemplate(templateJson);
        CsvDataParser csvParser = new CsvDataParser();
        Map<String, Object> data = csvParser.parse(csvData);
        return generatePdf(template, new DataContext(data));
    }

    /**
     * Get the underlying PdfRenderer for custom renderer registration.
     *
     * @return the PdfRenderer instance
     */
    public PdfRenderer getRenderer() {
        return pdfRenderer;
    }
}
