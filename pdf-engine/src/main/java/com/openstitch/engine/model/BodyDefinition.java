package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BodyDefinition {

    @JsonProperty("sections")
    private List<SectionDefinition> sections = new ArrayList<>();

    public BodyDefinition() {
    }

    public List<SectionDefinition> getSections() {
        return sections;
    }

    public void setSections(List<SectionDefinition> sections) {
        this.sections = sections;
    }

    /**
     * Migration support: if the JSON has "elements" instead of "sections",
     * wrap the elements into a single default section.
     */
    @JsonSetter("elements")
    public void setLegacyElements(List<Element> elements) {
        if (elements != null && (this.sections == null || this.sections.isEmpty())) {
            SectionDefinition defaultSection = new SectionDefinition();
            defaultSection.setId(UUID.randomUUID().toString());
            defaultSection.setName("Main");
            defaultSection.setElements(elements);
            this.sections = new ArrayList<>();
            this.sections.add(defaultSection);
        }
    }
}
