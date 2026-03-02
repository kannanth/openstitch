package com.openstitch.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PageNumberingTest {

    @Test
    void testTwoPassWithPageNumbering() {
        String template = """
            {
              "metadata": { "name": "Test", "version": 1 },
              "pageLayout": {
                "pageSize": "LETTER",
                "orientation": "PORTRAIT",
                "margins": { "top": 72, "right": 56, "bottom": 72, "left": 56 }
              },
              "pageNumbering": {
                "enabled": true,
                "format": "PAGE_X_OF_Y",
                "startFrom": 1
              },
              "body": {
                "sections": [
                  {
                    "id": "s1",
                    "name": "Main",
                    "elements": [
                      { "id": "b1", "type": "TEXT", "positioning": "FLOW", "content": "Page numbering test content" }
                    ]
                  }
                ]
              }
            }
            """;

        OpenStitchEngine engine = new OpenStitchEngine();
        byte[] pdf = engine.generatePdf(template, "{}");

        assertNotNull(pdf, "PDF should not be null");
        assertTrue(pdf.length > 500, "PDF should have content");
        assertEquals("%PDF-", new String(pdf, 0, 5), "Should be valid PDF");
    }

    @Test
    void testPageNumberExpression() {
        String template = """
            {
              "metadata": { "name": "Test", "version": 1 },
              "pageLayout": {
                "pageSize": "A4",
                "orientation": "PORTRAIT",
                "margins": { "top": 72, "right": 72, "bottom": 72, "left": 72 }
              },
              "pageNumbering": {
                "enabled": true,
                "format": "PAGE_X_OF_Y",
                "startFrom": 1
              },
              "body": {
                "sections": [
                  {
                    "id": "s1",
                    "name": "Main",
                    "elements": [
                      { "id": "b1", "type": "TEXT", "positioning": "FLOW", "content": "Current page: ${pageNumber}" }
                    ]
                  }
                ]
              }
            }
            """;

        OpenStitchEngine engine = new OpenStitchEngine();
        byte[] pdf = engine.generatePdf(template, "{}");

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "PDF with page number expression should have content");
    }

    @Test
    void testTotalPagesExpression() {
        String template = """
            {
              "metadata": { "name": "Test", "version": 1 },
              "pageLayout": {
                "pageSize": "A4",
                "orientation": "PORTRAIT",
                "margins": { "top": 72, "right": 72, "bottom": 72, "left": 72 }
              },
              "pageNumbering": {
                "enabled": true,
                "format": "PAGE_X_OF_Y",
                "startFrom": 1
              },
              "body": {
                "sections": [
                  {
                    "id": "s1",
                    "name": "Main",
                    "elements": [
                      { "id": "b1", "type": "TEXT", "positioning": "FLOW", "content": "Total pages: ${totalPages}" }
                    ]
                  }
                ]
              }
            }
            """;

        OpenStitchEngine engine = new OpenStitchEngine();
        byte[] pdf = engine.generatePdf(template, "{}");

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "PDF with totalPages expression should have content");
    }

    @Test
    void testPageNumberInFooter() {
        String template = """
            {
              "metadata": { "name": "Test", "version": 1 },
              "pageLayout": {
                "pageSize": "A4",
                "orientation": "PORTRAIT",
                "margins": { "top": 72, "right": 72, "bottom": 72, "left": 72 }
              },
              "pageNumbering": {
                "enabled": true,
                "format": "PAGE_X_OF_Y",
                "startFrom": 1
              },
              "footer": {
                "height": 30,
                "firstPageDifferent": false,
                "oddEvenDifferent": false,
                "defaultElements": [
                  { "id": "f1", "type": "TEXT", "positioning": "FLOW", "content": "Page ${pageNumber} of ${totalPages}" }
                ]
              },
              "body": {
                "sections": [
                  {
                    "id": "s1",
                    "name": "Main",
                    "elements": [
                      { "id": "b1", "type": "TEXT", "positioning": "FLOW", "content": "Content with page numbers in footer" }
                    ]
                  }
                ]
              }
            }
            """;

        OpenStitchEngine engine = new OpenStitchEngine();
        byte[] pdf = engine.generatePdf(template, "{}");

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "PDF with footer page numbers should have content");
    }

    @Test
    void testSinglePassWhenDisabled() {
        String template = """
            {
              "metadata": { "name": "Test", "version": 1 },
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
                      { "id": "b1", "type": "TEXT", "positioning": "FLOW", "content": "No page numbering" }
                    ]
                  }
                ]
              }
            }
            """;

        OpenStitchEngine engine = new OpenStitchEngine();
        byte[] pdf = engine.generatePdf(template, "{}");

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "PDF without page numbering should still work");
        assertEquals("%PDF-", new String(pdf, 0, 5), "Should be valid PDF");
    }
}
