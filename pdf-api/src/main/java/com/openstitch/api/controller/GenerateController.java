package com.openstitch.api.controller;

import com.openstitch.api.dto.GenerateRequest;
import com.openstitch.api.dto.GenerateWithIdRequest;
import com.openstitch.api.service.PdfGenerationService;
import com.openstitch.api.service.TemplateService;
import com.openstitch.engine.model.Template;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class GenerateController {

    private final PdfGenerationService pdfGenerationService;
    private final TemplateService templateService;

    public GenerateController(PdfGenerationService pdfGenerationService, TemplateService templateService) {
        this.pdfGenerationService = pdfGenerationService;
        this.templateService = templateService;
    }

    @PostMapping("/generate/inline")
    public ResponseEntity<byte[]> generateInline(@Valid @RequestBody GenerateRequest request) {
        byte[] pdfBytes = pdfGenerationService.generatePdf(
                request.getTemplate(), request.getData(),
                request.getDataString(), request.getDataFormat());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@Valid @RequestBody GenerateWithIdRequest request) {
        Template template = templateService.findTemplateById(request.getTemplateId());
        byte[] pdfBytes = pdfGenerationService.generatePdf(template, request.getData());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
