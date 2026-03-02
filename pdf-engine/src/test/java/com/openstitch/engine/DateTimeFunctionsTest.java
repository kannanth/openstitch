package com.openstitch.engine;

import com.openstitch.engine.expression.ExpressionEvaluator;
import com.openstitch.engine.parser.DataContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeFunctionsTest {

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final DataContext emptyContext = new DataContext(Collections.emptyMap());

    @Test
    void testToday() {
        String result = evaluator.evaluate("${today()}", emptyContext);
        // Should match ISO date format yyyy-MM-dd
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"), "today() should return ISO date, got: " + result);
        assertEquals(LocalDate.now().toString(), result);
    }

    @Test
    void testTime() {
        String result = evaluator.evaluate("${time()}", emptyContext);
        // Should match HH:mm:ss format
        assertNotNull(result);
        assertTrue(result.matches("\\d{2}:\\d{2}:\\d{2}"), "time() should return HH:mm:ss, got: " + result);
    }

    @Test
    void testTimeFormat() {
        String result = evaluator.evaluate("${timeFormat('2026-02-28T14:30:00', 'HH:mm')}", emptyContext);
        assertEquals("14:30", result);
    }

    @Test
    void testTimeFormatWithAmPm() {
        String result = evaluator.evaluate("${timeFormat('2026-02-28T14:30:00', 'hh:mm a')}", emptyContext);
        // Should produce something like "02:30 PM"
        assertNotNull(result);
        assertTrue(result.contains("30"), "Should contain minutes");
    }

    @Test
    void testTimeFormatWithTimeOnly() {
        String result = evaluator.evaluate("${timeFormat('14:30:00', 'HH:mm')}", emptyContext);
        assertEquals("14:30", result);
    }

    @Test
    void testDateFormatLocale() {
        String result = evaluator.evaluate("${dateFormatLocale('2026-02-28', 'MMMM', 'fr-FR')}", emptyContext);
        assertNotNull(result);
        // French month name for February
        assertTrue(result.toLowerCase().startsWith("f"), "French February should start with 'f', got: " + result);
    }

    @Test
    void testDateFormatLocaleGerman() {
        String result = evaluator.evaluate("${dateFormatLocale('2026-03-15', 'MMMM', 'de-DE')}", emptyContext);
        assertNotNull(result);
        // German month name for March is "März"
        assertTrue(result.toLowerCase().startsWith("m"), "German March should start with 'm', got: " + result);
    }

    @Test
    void testDateFormatInTemplate() {
        OpenStitchEngine engine = new OpenStitchEngine();

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
                      { "id": "b1", "type": "TEXT", "positioning": "FLOW", "content": "${dateFormat(now(), 'MM/dd/yyyy')}" }
                    ]
                  }
                ]
              }
            }
            """;

        byte[] pdf = engine.generatePdf(template, "{}");
        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "PDF with date expression should have content");
        assertEquals("%PDF-", new String(pdf, 0, 5), "Should be valid PDF");
    }

    @Test
    void testTodayInTemplate() {
        OpenStitchEngine engine = new OpenStitchEngine();

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
                      { "id": "b1", "type": "TEXT", "positioning": "FLOW", "content": "Today: ${today()}" }
                    ]
                  }
                ]
              }
            }
            """;

        byte[] pdf = engine.generatePdf(template, "{}");
        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
    }
}
