package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TextElement extends Element {

    @JsonProperty("content")
    private String content;

    public TextElement() {
        super(ElementType.TEXT);
    }

    public TextElement(String content) {
        super(ElementType.TEXT);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
