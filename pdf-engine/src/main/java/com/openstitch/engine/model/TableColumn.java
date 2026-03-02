package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TableColumn(
        @JsonProperty("header") String header,
        @JsonProperty("field") String field,
        @JsonProperty("width") Float width,
        @JsonProperty("alignment") Style.Alignment alignment,
        @JsonProperty("format") String format,
        @JsonProperty("wrapText") Boolean wrapText,
        @JsonProperty("formatPreset") String formatPreset,
        @JsonProperty("conditionalFormats") List<ConditionalFormat> conditionalFormats
) {

    public TableColumn(String header, String field) {
        this(header, field, null, Style.Alignment.LEFT, null, null, null, null);
    }

    /**
     * Null-safe convenience method that defaults to false.
     */
    public boolean isWrapText() {
        return wrapText != null && wrapText;
    }
}
