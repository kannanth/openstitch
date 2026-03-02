package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PageLayout {

    @JsonProperty("pageSize")
    private PageSize pageSize = PageSize.A4;

    @JsonProperty("orientation")
    private Orientation orientation = Orientation.PORTRAIT;

    @JsonProperty("margins")
    private Margins margins = Margins.defaultMargins();

    @JsonProperty("customWidth")
    private Float customWidth;

    @JsonProperty("customHeight")
    private Float customHeight;

    public PageLayout() {
    }

    public PageLayout(PageSize pageSize, Orientation orientation, Margins margins,
                      Float customWidth, Float customHeight) {
        this.pageSize = pageSize;
        this.orientation = orientation;
        this.margins = margins;
        this.customWidth = customWidth;
        this.customHeight = customHeight;
    }

    /**
     * Returns the effective page width in points, considering orientation.
     * For CUSTOM page size, uses customWidth. For LANDSCAPE, swaps width and height.
     */
    public float getEffectiveWidth() {
        float width;
        if (pageSize == PageSize.CUSTOM) {
            width = customWidth != null ? customWidth : 0;
        } else {
            width = pageSize.getWidth();
        }

        if (orientation == Orientation.LANDSCAPE) {
            float height;
            if (pageSize == PageSize.CUSTOM) {
                height = customHeight != null ? customHeight : 0;
            } else {
                height = pageSize.getHeight();
            }
            return height;
        }

        return width;
    }

    /**
     * Returns the effective page height in points, considering orientation.
     * For CUSTOM page size, uses customHeight. For LANDSCAPE, swaps width and height.
     */
    public float getEffectiveHeight() {
        float height;
        if (pageSize == PageSize.CUSTOM) {
            height = customHeight != null ? customHeight : 0;
        } else {
            height = pageSize.getHeight();
        }

        if (orientation == Orientation.LANDSCAPE) {
            float width;
            if (pageSize == PageSize.CUSTOM) {
                width = customWidth != null ? customWidth : 0;
            } else {
                width = pageSize.getWidth();
            }
            return width;
        }

        return height;
    }

    public PageSize getPageSize() {
        return pageSize;
    }

    public void setPageSize(PageSize pageSize) {
        this.pageSize = pageSize;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Margins getMargins() {
        return margins;
    }

    public void setMargins(Margins margins) {
        this.margins = margins;
    }

    public Float getCustomWidth() {
        return customWidth;
    }

    public void setCustomWidth(Float customWidth) {
        this.customWidth = customWidth;
    }

    public Float getCustomHeight() {
        return customHeight;
    }

    public void setCustomHeight(Float customHeight) {
        this.customHeight = customHeight;
    }
}
