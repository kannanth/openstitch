package com.openstitch.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextElement.class, name = "TEXT"),
        @JsonSubTypes.Type(value = TableElement.class, name = "TABLE"),
        @JsonSubTypes.Type(value = ImageElement.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = ChartElement.class, name = "CHART"),
        @JsonSubTypes.Type(value = ConditionalBlock.class, name = "CONDITIONAL"),
        @JsonSubTypes.Type(value = RepeatingSection.class, name = "REPEATING_SECTION")
})
public abstract class Element {

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private ElementType type;

    @JsonProperty("position")
    private Position position;

    @JsonProperty("dimension")
    private Dimension dimension;

    @JsonProperty("positioning")
    private Positioning positioning = Positioning.FLOW;

    @JsonProperty("style")
    private Style style;

    @JsonProperty("marginTop")
    private float marginTop;

    @JsonProperty("marginBottom")
    private float marginBottom;

    public enum Positioning {
        FLOW,
        ABSOLUTE
    }

    protected Element() {
    }

    protected Element(ElementType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ElementType getType() {
        return type;
    }

    public void setType(ElementType type) {
        this.type = type;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public Positioning getPositioning() {
        return positioning;
    }

    public void setPositioning(Positioning positioning) {
        this.positioning = positioning;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public float getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(float marginTop) {
        this.marginTop = marginTop;
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(float marginBottom) {
        this.marginBottom = marginBottom;
    }
}
