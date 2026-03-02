package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FontSpec(
        @JsonProperty("family") String family,
        @JsonProperty("size") float size,
        @JsonProperty("bold") boolean bold,
        @JsonProperty("italic") boolean italic,
        @JsonProperty("color") String color
) {

    public FontSpec() {
        this("Helvetica", 12, false, false, "#000000");
    }
}
