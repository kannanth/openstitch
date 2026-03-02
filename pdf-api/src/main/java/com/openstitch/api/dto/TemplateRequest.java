package com.openstitch.api.dto;

import jakarta.validation.constraints.NotNull;

public class TemplateRequest {

    @NotNull
    private Object template;

    public TemplateRequest() {
    }

    public Object getTemplate() {
        return template;
    }

    public void setTemplate(Object template) {
        this.template = template;
    }
}
