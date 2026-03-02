package com.openstitch.api.dto;

public class HealthResponse {

    private String status;
    private String version;

    public HealthResponse(String status, String version) {
        this.status = status;
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public String getVersion() {
        return version;
    }
}
