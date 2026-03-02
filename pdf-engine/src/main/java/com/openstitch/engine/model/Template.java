package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Template {

    @JsonProperty("metadata")
    private TemplateMetadata metadata;

    @JsonProperty("pageLayout")
    private PageLayout pageLayout;

    @JsonProperty("header")
    private HeaderDefinition header;

    @JsonProperty("footer")
    private FooterDefinition footer;

    @JsonProperty("pageNumbering")
    private PageNumbering pageNumbering;

    @JsonProperty("body")
    private BodyDefinition body;

    public Template() {
    }

    public TemplateMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(TemplateMetadata metadata) {
        this.metadata = metadata;
    }

    public PageLayout getPageLayout() {
        return pageLayout;
    }

    public void setPageLayout(PageLayout pageLayout) {
        this.pageLayout = pageLayout;
    }

    public HeaderDefinition getHeader() {
        return header;
    }

    public void setHeader(HeaderDefinition header) {
        this.header = header;
    }

    public FooterDefinition getFooter() {
        return footer;
    }

    public void setFooter(FooterDefinition footer) {
        this.footer = footer;
    }

    public PageNumbering getPageNumbering() {
        return pageNumbering;
    }

    public void setPageNumbering(PageNumbering pageNumbering) {
        this.pageNumbering = pageNumbering;
    }

    public BodyDefinition getBody() {
        return body;
    }

    public void setBody(BodyDefinition body) {
        this.body = body;
    }
}
