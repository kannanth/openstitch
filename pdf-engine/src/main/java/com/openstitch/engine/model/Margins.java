package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Margins(
        @JsonProperty("top") float top,
        @JsonProperty("right") float right,
        @JsonProperty("bottom") float bottom,
        @JsonProperty("left") float left
) {

    public Margins() {
        this(72, 72, 72, 72);
    }

    /**
     * Returns default margins of 72 points (1 inch) on all sides.
     */
    public static Margins defaultMargins() {
        return new Margins(72, 72, 72, 72);
    }
}
