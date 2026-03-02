package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SectionDefinition {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("sectionHeader")
    private SectionBand sectionHeader;

    @JsonProperty("elements")
    private List<Element> elements = new ArrayList<>();

    @JsonProperty("sectionFooter")
    private SectionBand sectionFooter;

    @JsonProperty("repeatHeaderOnPageBreak")
    private boolean repeatHeaderOnPageBreak;

    @JsonProperty("repeatFooterOnPageBreak")
    private boolean repeatFooterOnPageBreak;

    @JsonProperty("dataSource")
    private String dataSource;

    public SectionDefinition() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SectionBand getSectionHeader() {
        return sectionHeader;
    }

    public void setSectionHeader(SectionBand sectionHeader) {
        this.sectionHeader = sectionHeader;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public SectionBand getSectionFooter() {
        return sectionFooter;
    }

    public void setSectionFooter(SectionBand sectionFooter) {
        this.sectionFooter = sectionFooter;
    }

    public boolean isRepeatHeaderOnPageBreak() {
        return repeatHeaderOnPageBreak;
    }

    public void setRepeatHeaderOnPageBreak(boolean repeatHeaderOnPageBreak) {
        this.repeatHeaderOnPageBreak = repeatHeaderOnPageBreak;
    }

    public boolean isRepeatFooterOnPageBreak() {
        return repeatFooterOnPageBreak;
    }

    public void setRepeatFooterOnPageBreak(boolean repeatFooterOnPageBreak) {
        this.repeatFooterOnPageBreak = repeatFooterOnPageBreak;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
