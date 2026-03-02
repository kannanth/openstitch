package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeaderDefinition {

    @JsonProperty("height")
    private float height = 50;

    @JsonProperty("firstPageDifferent")
    private boolean firstPageDifferent;

    @JsonProperty("oddEvenDifferent")
    private boolean oddEvenDifferent;

    @JsonProperty("defaultElements")
    private List<Element> defaultElements;

    @JsonProperty("firstPageElements")
    private List<Element> firstPageElements;

    @JsonProperty("oddPageElements")
    private List<Element> oddPageElements;

    @JsonProperty("evenPageElements")
    private List<Element> evenPageElements;

    public HeaderDefinition() {
    }

    /**
     * Returns the appropriate element list for the given page number.
     *
     * @param pageNumber  the current page number (1-based)
     * @param totalPages  the total number of pages
     * @return the list of elements to render for this page
     */
    public List<Element> getElementsForPage(int pageNumber, int totalPages) {
        if (firstPageDifferent && pageNumber == 1 && firstPageElements != null) {
            return firstPageElements;
        }

        if (oddEvenDifferent) {
            if (pageNumber % 2 == 0 && evenPageElements != null) {
                return evenPageElements;
            }
            if (pageNumber % 2 != 0 && oddPageElements != null) {
                return oddPageElements;
            }
        }

        return defaultElements != null ? defaultElements : Collections.emptyList();
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isFirstPageDifferent() {
        return firstPageDifferent;
    }

    public void setFirstPageDifferent(boolean firstPageDifferent) {
        this.firstPageDifferent = firstPageDifferent;
    }

    public boolean isOddEvenDifferent() {
        return oddEvenDifferent;
    }

    public void setOddEvenDifferent(boolean oddEvenDifferent) {
        this.oddEvenDifferent = oddEvenDifferent;
    }

    public List<Element> getDefaultElements() {
        return defaultElements;
    }

    public void setDefaultElements(List<Element> defaultElements) {
        this.defaultElements = defaultElements;
    }

    public List<Element> getFirstPageElements() {
        return firstPageElements;
    }

    public void setFirstPageElements(List<Element> firstPageElements) {
        this.firstPageElements = firstPageElements;
    }

    public List<Element> getOddPageElements() {
        return oddPageElements;
    }

    public void setOddPageElements(List<Element> oddPageElements) {
        this.oddPageElements = oddPageElements;
    }

    public List<Element> getEvenPageElements() {
        return evenPageElements;
    }

    public void setEvenPageElements(List<Element> evenPageElements) {
        this.evenPageElements = evenPageElements;
    }
}
