package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Padding(
        @JsonProperty("top") float top,
        @JsonProperty("right") float right,
        @JsonProperty("bottom") float bottom,
        @JsonProperty("left") float left
) {

    public Padding() {
        this(0, 0, 0, 0);
    }

    public static Padding of(float all) {
        return new Padding(all, all, all, all);
    }

    public static Padding of(float vertical, float horizontal) {
        return new Padding(vertical, horizontal, vertical, horizontal);
    }
}
