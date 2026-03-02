package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Border(
        @JsonProperty("width") float width,
        @JsonProperty("color") String color,
        @JsonProperty("style") BorderStyle style
) {

    public Border() {
        this(1.0f, "#000000", BorderStyle.SOLID);
    }

    public enum BorderStyle {
        SOLID,
        DASHED,
        DOTTED,
        NONE
    }
}
