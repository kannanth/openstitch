package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Style(
        @JsonProperty("fontFamily") String fontFamily,
        @JsonProperty("fontSize") float fontSize,
        @JsonProperty("bold") boolean bold,
        @JsonProperty("italic") boolean italic,
        @JsonProperty("underline") boolean underline,
        @JsonProperty("textColor") String textColor,
        @JsonProperty("backgroundColor") String backgroundColor,
        @JsonProperty("alignment") Alignment alignment,
        @JsonProperty("lineHeight") float lineHeight,
        @JsonProperty("padding") Padding padding
) {

    public Style() {
        this("Helvetica", 12, false, false, false, "#000000", null,
                Alignment.LEFT, 1.2f, new Padding());
    }

    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT,
        JUSTIFIED
    }
}
