package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConditionalFormat(
        @JsonProperty("condition") String condition,
        @JsonProperty("style") Style style
) {
}
