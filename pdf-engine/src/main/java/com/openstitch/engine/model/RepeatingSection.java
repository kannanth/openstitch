package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepeatingSection extends Element {

    @JsonProperty("dataSource")
    private String dataSource;

    @JsonProperty("elements")
    private List<Element> elements;

    @JsonProperty("separator")
    private Separator separator;

    @JsonProperty("pageBreakBetween")
    private boolean pageBreakBetween = false;

    public RepeatingSection() {
        super(ElementType.REPEATING_SECTION);
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public Separator getSeparator() {
        return separator;
    }

    public void setSeparator(Separator separator) {
        this.separator = separator;
    }

    public boolean isPageBreakBetween() {
        return pageBreakBetween;
    }

    public void setPageBreakBetween(boolean pageBreakBetween) {
        this.pageBreakBetween = pageBreakBetween;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Separator(
            @JsonProperty("height") float height,
            @JsonProperty("color") String color
    ) {
    }
}
