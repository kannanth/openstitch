package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConditionalBlock extends Element {

    @JsonProperty("condition")
    private String condition;

    @JsonProperty("thenElements")
    private List<Element> thenElements;

    @JsonProperty("elseElements")
    private List<Element> elseElements;

    public ConditionalBlock() {
        super(ElementType.CONDITIONAL);
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public List<Element> getThenElements() {
        return thenElements;
    }

    public void setThenElements(List<Element> thenElements) {
        this.thenElements = thenElements;
    }

    public List<Element> getElseElements() {
        return elseElements;
    }

    public void setElseElements(List<Element> elseElements) {
        this.elseElements = elseElements;
    }
}
