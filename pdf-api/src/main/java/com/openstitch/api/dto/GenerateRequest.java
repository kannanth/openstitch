package com.openstitch.api.dto;

import jakarta.validation.constraints.NotNull;

public class GenerateRequest {

    @NotNull
    private Object template;  // Template JSON object

    private Object data;  // Data object (JSON)

    private String dataFormat;  // JSON, XML, CSV - defaults to JSON

    private String dataString;  // Raw data string (for XML/CSV)

    public GenerateRequest() {
    }

    public Object getTemplate() {
        return template;
    }

    public void setTemplate(Object template) {
        this.template = template;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }
}
