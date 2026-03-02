package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageElement extends Element {

    @JsonProperty("source")
    private ImageSource source;

    @JsonProperty("data")
    private String data;

    @JsonProperty("fit")
    private ImageFit fit = ImageFit.CONTAIN;

    public ImageElement() {
        super(ElementType.IMAGE);
    }

    public ImageSource getSource() {
        return source;
    }

    public void setSource(ImageSource source) {
        this.source = source;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ImageFit getFit() {
        return fit;
    }

    public void setFit(ImageFit fit) {
        this.fit = fit;
    }
}
