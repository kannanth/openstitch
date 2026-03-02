package com.openstitch.engine.model;

public enum PageNumberFormat {

    PAGE_X_OF_Y("Page %d of %d"),
    PAGE_X("%d"),
    ROMAN(null),
    CUSTOM(null);

    private final String formatPattern;

    PageNumberFormat(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    public String getFormatPattern() {
        return formatPattern;
    }
}
