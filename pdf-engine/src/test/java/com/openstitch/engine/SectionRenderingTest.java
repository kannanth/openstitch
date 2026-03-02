package com.openstitch.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SectionRenderingTest {

    private static final String TEMPLATE_JSON = """
        {
          "metadata": { "name": "Test", "version": 1 },
          "pageLayout": {
            "pageSize": "LETTER",
            "orientation": "PORTRAIT",
            "margins": { "top": 72, "right": 56, "bottom": 72, "left": 56 }
          },
          "header": {
            "height": 60,
            "firstPageDifferent": false,
            "oddEvenDifferent": false,
            "defaultElements": [
              { "id": "h1", "type": "TEXT", "positioning": "FLOW", "content": "${company.name}" }
            ]
          },
          "body": {
            "sections": [
              {
                "id": "s1",
                "name": "Main",
                "elements": [
                  {
                    "id": "b1",
                    "type": "TEXT",
                    "positioning": "FLOW",
                    "content": "Hello from sections!"
                  },
                  {
                    "id": "b2",
                    "type": "TABLE",
                    "positioning": "FLOW",
                    "dataSource": "items",
                    "showHeader": true,
                    "columns": [
                      { "header": "Name", "field": "name" },
                      { "header": "Value", "field": "value", "alignment": "RIGHT" }
                    ]
                  }
                ]
              }
            ]
          },
          "footer": {
            "height": 30,
            "firstPageDifferent": false,
            "oddEvenDifferent": false,
            "defaultElements": [
              { "id": "f1", "type": "TEXT", "positioning": "FLOW", "content": "Footer" }
            ]
          }
        }
        """;

    private static final String DATA_JSON = """
        {
          "company": { "name": "Test Corp" },
          "items": [
            { "name": "Item A", "value": 100 },
            { "name": "Item B", "value": 200 },
            { "name": "Item C", "value": 300 }
          ]
        }
        """;

    @Test
    void testSectionRendering() {
        OpenStitchEngine engine = new OpenStitchEngine();
        byte[] pdf = engine.generatePdf(TEMPLATE_JSON, DATA_JSON);

        assertNotNull(pdf, "PDF should not be null");
        assertTrue(pdf.length > 1000, "PDF should have substantial content, got " + pdf.length + " bytes");

        // Verify it's a valid PDF (starts with %PDF)
        String header = new String(pdf, 0, 5);
        assertEquals("%PDF-", header, "Should be valid PDF");
    }

    @Test
    void testSectionWithTextOnlyRendering() {
        String simpleTemplate = """
            {
              "metadata": { "name": "Simple", "version": 1 },
              "pageLayout": {
                "pageSize": "A4",
                "orientation": "PORTRAIT",
                "margins": { "top": 72, "right": 72, "bottom": 72, "left": 72 }
              },
              "body": {
                "sections": [
                  {
                    "id": "s1",
                    "name": "Main",
                    "elements": [
                      { "id": "b1", "type": "TEXT", "positioning": "FLOW", "content": "Hello World" }
                    ]
                  }
                ]
              }
            }
            """;

        OpenStitchEngine engine = new OpenStitchEngine();
        byte[] pdf = engine.generatePdf(simpleTemplate, "{}");

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "PDF with text should have content, got " + pdf.length + " bytes");
    }

    @Test
    void testLegacyFormatStillWorks() {
        String legacyTemplate = """
            {
              "metadata": { "name": "Legacy", "version": 1 },
              "pageLayout": {
                "pageSize": "A4",
                "orientation": "PORTRAIT",
                "margins": { "top": 72, "right": 72, "bottom": 72, "left": 72 }
              },
              "body": {
                "elements": [
                  { "id": "b1", "type": "TEXT", "positioning": "FLOW", "content": "Legacy body text" }
                ]
              }
            }
            """;

        OpenStitchEngine engine = new OpenStitchEngine();
        byte[] pdf = engine.generatePdf(legacyTemplate, "{}");

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "Legacy template should still work, got " + pdf.length + " bytes");
    }
}
