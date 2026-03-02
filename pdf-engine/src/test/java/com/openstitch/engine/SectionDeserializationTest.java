package com.openstitch.engine;

import com.openstitch.engine.model.Template;
import com.openstitch.engine.model.SectionDefinition;
import com.openstitch.engine.parser.TemplateParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SectionDeserializationTest {

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
              { "id": "h1", "type": "TEXT", "positioning": "FLOW", "content": "Header" }
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
                    "content": "Hello World"
                  },
                  {
                    "id": "b2",
                    "type": "TABLE",
                    "positioning": "FLOW",
                    "dataSource": "items",
                    "showHeader": true,
                    "columns": [
                      { "header": "Name", "field": "name" }
                    ]
                  }
                ]
              }
            ]
          }
        }
        """;

    @Test
    void testSectionsDeserialization() {
        TemplateParser parser = new TemplateParser();
        Template template = parser.parse(TEMPLATE_JSON);

        assertNotNull(template.getBody(), "body should not be null");
        assertNotNull(template.getBody().getSections(), "sections should not be null");
        assertEquals(1, template.getBody().getSections().size(), "should have 1 section");

        SectionDefinition section = template.getBody().getSections().get(0);
        assertEquals("s1", section.getId());
        assertEquals("Main", section.getName());
        assertNotNull(section.getElements(), "section elements should not be null");
        assertEquals(2, section.getElements().size(), "section should have 2 elements");
        assertEquals("TEXT", section.getElements().get(0).getType().name());
        assertEquals("TABLE", section.getElements().get(1).getType().name());
    }

    @Test
    void testLegacyElementsMigration() {
        String legacyJson = """
            {
              "metadata": { "name": "Legacy", "version": 1 },
              "pageLayout": {
                "pageSize": "A4",
                "orientation": "PORTRAIT",
                "margins": { "top": 72, "right": 72, "bottom": 72, "left": 72 }
              },
              "body": {
                "elements": [
                  { "id": "e1", "type": "TEXT", "positioning": "FLOW", "content": "Legacy" }
                ]
              }
            }
            """;

        TemplateParser parser = new TemplateParser();
        Template template = parser.parse(legacyJson);

        assertNotNull(template.getBody().getSections());
        assertEquals(1, template.getBody().getSections().size());
        assertEquals("Main", template.getBody().getSections().get(0).getName());
        assertEquals(1, template.getBody().getSections().get(0).getElements().size());
    }
}
