package com.openstitch.engine.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.openstitch.engine.exception.StorageException;
import com.openstitch.engine.model.Template;
import com.openstitch.engine.model.TemplateMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystemStorageProvider implements StorageProvider {

    private static final Logger log = LoggerFactory.getLogger(FileSystemStorageProvider.class);

    private final Path baseDir;
    private final ObjectMapper objectMapper;

    public FileSystemStorageProvider(String baseDir) {
        this.baseDir = Path.of(baseDir);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        ensureBaseDir();
    }

    private void ensureBaseDir() {
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new StorageException("Failed to create storage directory: " + baseDir, e);
        }
    }

    private Path templateFile(String id) {
        return baseDir.resolve(id + ".json");
    }

    private Path metadataFile(String id) {
        return baseDir.resolve(id + ".meta.json");
    }

    @Override
    public Template save(Template template) {
        if (template.getMetadata() == null) {
            template.setMetadata(new TemplateMetadata());
        }

        TemplateMetadata metadata = template.getMetadata();
        if (metadata.getId() == null || metadata.getId().isBlank()) {
            metadata.setId(UUID.randomUUID().toString());
        }

        Instant now = Instant.now();
        metadata.setCreatedAt(now);
        metadata.setUpdatedAt(now);
        if (metadata.getVersion() < 1) {
            metadata.setVersion(1);
        }

        String id = metadata.getId();
        writeTemplate(id, template);
        writeMetadata(id, metadata);

        log.debug("Saved template with id: {}", id);
        return template;
    }

    @Override
    public Optional<Template> findById(String id) {
        Path file = templateFile(id);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            Template template = objectMapper.readValue(file.toFile(), Template.class);
            return Optional.of(template);
        } catch (IOException e) {
            throw new StorageException("Failed to read template: " + id, e);
        }
    }

    @Override
    public List<TemplateMetadata> findAll() {
        try (Stream<Path> files = Files.list(baseDir)) {
            return files
                    .filter(p -> p.getFileName().toString().endsWith(".meta.json"))
                    .map(this::readMetadataFile)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new StorageException("Failed to list templates", e);
        }
    }

    @Override
    public List<TemplateMetadata> findByTag(String tag) {
        return findAll().stream()
                .filter(m -> m.getTags() != null && m.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    @Override
    public Template update(String id, Template template) {
        Path existingFile = templateFile(id);
        if (!Files.exists(existingFile)) {
            throw new StorageException("Template not found: " + id);
        }

        Template existing;
        try {
            existing = objectMapper.readValue(existingFile.toFile(), Template.class);
        } catch (IOException e) {
            throw new StorageException("Failed to read existing template: " + id, e);
        }

        TemplateMetadata existingMeta = existing.getMetadata();
        TemplateMetadata incomingMeta = template.getMetadata();

        if (incomingMeta != null) {
            if (incomingMeta.getName() != null) {
                existingMeta.setName(incomingMeta.getName());
            }
            if (incomingMeta.getDescription() != null) {
                existingMeta.setDescription(incomingMeta.getDescription());
            }
            if (incomingMeta.getAuthor() != null) {
                existingMeta.setAuthor(incomingMeta.getAuthor());
            }
            if (incomingMeta.getTags() != null) {
                existingMeta.setTags(incomingMeta.getTags());
            }
        }

        existingMeta.setUpdatedAt(Instant.now());
        existingMeta.setVersion(existingMeta.getVersion() + 1);

        // Merge template content fields
        if (template.getPageLayout() != null) {
            existing.setPageLayout(template.getPageLayout());
        }
        if (template.getHeader() != null) {
            existing.setHeader(template.getHeader());
        }
        if (template.getFooter() != null) {
            existing.setFooter(template.getFooter());
        }
        if (template.getPageNumbering() != null) {
            existing.setPageNumbering(template.getPageNumbering());
        }
        if (template.getBody() != null) {
            existing.setBody(template.getBody());
        }

        existing.setMetadata(existingMeta);

        writeTemplate(id, existing);
        writeMetadata(id, existingMeta);

        log.debug("Updated template with id: {}", id);
        return existing;
    }

    @Override
    public void delete(String id) {
        try {
            Files.deleteIfExists(templateFile(id));
            Files.deleteIfExists(metadataFile(id));
            log.debug("Deleted template with id: {}", id);
        } catch (IOException e) {
            throw new StorageException("Failed to delete template: " + id, e);
        }
    }

    @Override
    public boolean exists(String id) {
        return Files.exists(templateFile(id));
    }

    private void writeTemplate(String id, Template template) {
        try {
            objectMapper.writeValue(templateFile(id).toFile(), template);
        } catch (IOException e) {
            throw new StorageException("Failed to write template: " + id, e);
        }
    }

    private void writeMetadata(String id, TemplateMetadata metadata) {
        try {
            objectMapper.writeValue(metadataFile(id).toFile(), metadata);
        } catch (IOException e) {
            throw new StorageException("Failed to write metadata: " + id, e);
        }
    }

    private TemplateMetadata readMetadataFile(Path file) {
        try {
            return objectMapper.readValue(file.toFile(), TemplateMetadata.class);
        } catch (IOException e) {
            log.warn("Failed to read metadata file: {}", file, e);
            return null;
        }
    }
}
