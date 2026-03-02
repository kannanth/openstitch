package com.openstitch.api.controller;

import com.openstitch.api.dto.TemplateRequest;
import com.openstitch.api.dto.TemplateResponse;
import com.openstitch.api.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    public ResponseEntity<TemplateResponse> create(@Valid @RequestBody TemplateRequest request) {
        TemplateResponse response = templateService.save(request.getTemplate());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TemplateResponse>> list(@RequestParam(required = false) String tag) {
        List<TemplateResponse> responses;
        if (tag != null && !tag.isBlank()) {
            responses = templateService.findByTag(tag);
        } else {
            responses = templateService.findAll();
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getById(@PathVariable String id) {
        TemplateResponse response = templateService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> update(@PathVariable String id,
                                                   @Valid @RequestBody TemplateRequest request) {
        TemplateResponse response = templateService.update(id, request.getTemplate());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<TemplateResponse> clone(@PathVariable String id) {
        TemplateResponse response = templateService.clone(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
