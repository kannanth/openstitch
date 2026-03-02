package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TableElement extends Element {

    @JsonProperty("dataSource")
    private String dataSource;

    @JsonProperty("columns")
    private List<TableColumn> columns;

    @JsonProperty("headerStyle")
    private Style headerStyle;

    @JsonProperty("rowStyle")
    private Style rowStyle;

    @JsonProperty("alternateRowColor")
    private String alternateRowColor;

    @JsonProperty("borderStyle")
    private Border borderStyle;

    @JsonProperty("footerCells")
    private List<TableFooterCell> footerCells;

    @JsonProperty("showHeader")
    private boolean showHeader = true;

    public TableElement() {
        super(ElementType.TABLE);
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<TableColumn> columns) {
        this.columns = columns;
    }

    public Style getHeaderStyle() {
        return headerStyle;
    }

    public void setHeaderStyle(Style headerStyle) {
        this.headerStyle = headerStyle;
    }

    public Style getRowStyle() {
        return rowStyle;
    }

    public void setRowStyle(Style rowStyle) {
        this.rowStyle = rowStyle;
    }

    public String getAlternateRowColor() {
        return alternateRowColor;
    }

    public void setAlternateRowColor(String alternateRowColor) {
        this.alternateRowColor = alternateRowColor;
    }

    public Border getBorderStyle() {
        return borderStyle;
    }

    public void setBorderStyle(Border borderStyle) {
        this.borderStyle = borderStyle;
    }

    public List<TableFooterCell> getFooterCells() {
        return footerCells;
    }

    public void setFooterCells(List<TableFooterCell> footerCells) {
        this.footerCells = footerCells;
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
    }
}
