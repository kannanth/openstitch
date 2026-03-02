package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartElement extends Element {

    @JsonProperty("chartType")
    private ChartType chartType;

    @JsonProperty("dataSource")
    private String dataSource;

    @JsonProperty("categoryField")
    private String categoryField;

    @JsonProperty("valueFields")
    private List<String> valueFields;

    @JsonProperty("title")
    private String title;

    @JsonProperty("showLegend")
    private boolean showLegend = true;

    @JsonProperty("axisLabels")
    private Map<String, String> axisLabels;

    @JsonProperty("colors")
    private List<String> colors;

    public ChartElement() {
        super(ElementType.CHART);
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getCategoryField() {
        return categoryField;
    }

    public void setCategoryField(String categoryField) {
        this.categoryField = categoryField;
    }

    public List<String> getValueFields() {
        return valueFields;
    }

    public void setValueFields(List<String> valueFields) {
        this.valueFields = valueFields;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isShowLegend() {
        return showLegend;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    public Map<String, String> getAxisLabels() {
        return axisLabels;
    }

    public void setAxisLabels(Map<String, String> axisLabels) {
        this.axisLabels = axisLabels;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }
}
