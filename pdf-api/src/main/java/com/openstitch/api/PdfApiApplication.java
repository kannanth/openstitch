package com.openstitch.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;

// Exclude DB auto-config for now - will be enabled when storage layer is added
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, FlywayAutoConfiguration.class})
public class PdfApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PdfApiApplication.class, args);
    }
}
