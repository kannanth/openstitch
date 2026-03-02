package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TableFooterCell(
        @JsonProperty("content") String content,
        @JsonProperty("colSpan") int colSpan
) {

    public TableFooterCell(String content) {
        this(content, 1);
    }
}
