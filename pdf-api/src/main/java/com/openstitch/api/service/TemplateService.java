package com.openstitch.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstitch.api.dto.TemplateResponse;
import com.openstitch.api.exception.ResourceNotFoundException;
import com.openstitch.engine.model.Template;
import com.openstitch.engine.model.TemplateMetadata;
import com.openstitch.engine.storage.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

    private final StorageProvider storageProvider;
    private final ObjectMapper objectMapper;

    public TemplateService(StorageProvider storageProvider, ObjectMapper objectMapper) {
        this.storageProvider = storageProvider;
        this.objectMapper = objectMapper;
    }

    public TemplateResponse save(Object templateObj) {
        Template template = objectMapper.convertValue(templateObj, Template.class);
        Template saved = storageProvider.save(template);
        log.debug("Template saved with id: {}", saved.getMetadata().getId());
        return toResponse(saved);
    }

    public TemplateResponse findById(String id) {
        Template template = storageProvider.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
        return toResponse(template);
    }

    public Template findTemplateById(String id) {
        return storageProvider.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
    }

    public List<TemplateResponse> findAll() {
        return storageProvider.findAll().stream()
                .map(this::toMetadataResponse)
                .collect(Collectors.toList());
    }

    public List<TemplateResponse> findByTag(String tag) {
        return storageProvider.findByTag(tag).stream()
                .map(this::toMetadataResponse)
                .collect(Collectors.toList());
    }

    public TemplateResponse update(String id, Object templateObj) {
        Template template = objectMapper.convertValue(templateObj, Template.class);
        Template updated = storageProvider.update(id, template);
        log.debug("Template updated with id: {}", id);
        return toResponse(updated);
    }

    public void delete(String id) {
        if (!storageProvider.exists(id)) {
            throw new ResourceNotFoundException("Template not found: " + id);
        }
        storageProvider.delete(id);
        log.debug("Template deleted with id: {}", id);
    }

    public TemplateResponse clone(String id) {
        Template original = storageProvider.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));

        // Deep copy via serialization
        Template cloned = objectMapper.convertValue(
                objectMapper.convertValue(original, Object.class), Template.class);

        TemplateMetadata meta = cloned.getMetadata();
        if (meta == null) {
            meta = new TemplateMetadata();
            cloned.setMetadata(meta);
        }
        meta.setId(null); // will be regenerated on save
        meta.setName(meta.getName() != null ? meta.getName() + " (Copy)" : "Copy");
        meta.setVersion(1);
        meta.setCreatedAt(null);
        meta.setUpdatedAt(null);

        Template saved = storageProvider.save(cloned);
        log.debug("Template cloned from {} to {}", id, saved.getMetadata().getId());
        return toResponse(saved);
    }

    private TemplateResponse toResponse(Template template) {
        TemplateMetadata meta = template.getMetadata();
        return new TemplateResponse(
                meta != null ? meta.getId() : null,
                meta != null ? meta.getName() : null,
                meta != null ? meta.getDescription() : null,
                meta != null ? meta.getAuthor() : null,
                meta != null ? meta.getTags() : null,
                objectMapper.convertValue(template, Object.class),
                meta != null ? meta.getVersion() : 1,
                meta != null ? meta.getCreatedAt() : null,
                meta != null ? meta.getUpdatedAt() : null
        );
    }

    private TemplateResponse toMetadataResponse(TemplateMetadata meta) {
        return new TemplateResponse(
                meta.getId(),
                meta.getName(),
                meta.getDescription(),
                meta.getAuthor(),
                meta.getTags(),
                null,
                meta.getVersion(),
                meta.getCreatedAt(),
                meta.getUpdatedAt()
        );
    }
}
