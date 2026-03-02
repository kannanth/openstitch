package com.openstitch.api.dto;

import jakarta.validation.constraints.NotBlank;

public class GenerateWithIdRequest {

    @NotBlank
    private String templateId;

    private Object data;

    public GenerateWithIdRequest() {
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
