package com.openstitch.engine.model;

public enum PageSize {

    A4(595.28f, 841.89f),
    LETTER(612f, 792f),
    LEGAL(612f, 1008f),
    CUSTOM(0f, 0f);

    private final float width;
    private final float height;

    PageSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
