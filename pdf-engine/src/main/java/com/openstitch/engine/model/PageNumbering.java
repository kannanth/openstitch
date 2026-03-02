package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PageNumbering {

    @JsonProperty("enabled")
    private boolean enabled = false;

    @JsonProperty("format")
    private PageNumberFormat format;

    @JsonProperty("position")
    private Position position;

    @JsonProperty("startFrom")
    private int startFrom = 1;

    @JsonProperty("style")
    private Style style;

    @JsonProperty("customFormat")
    private String customFormat;

    public PageNumbering() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public PageNumberFormat getFormat() {
        return format;
    }

    public void setFormat(PageNumberFormat format) {
        this.format = format;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(int startFrom) {
        this.startFrom = startFrom;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public String getCustomFormat() {
        return customFormat;
    }

    public void setCustomFormat(String customFormat) {
        this.customFormat = customFormat;
    }
}
