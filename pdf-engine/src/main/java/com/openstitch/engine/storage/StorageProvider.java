package com.openstitch.engine.storage;

import com.openstitch.engine.model.Template;
import com.openstitch.engine.model.TemplateMetadata;

import java.util.List;
import java.util.Optional;

public interface StorageProvider {
    Template save(Template template);
    Optional<Template> findById(String id);
    List<TemplateMetadata> findAll();
    List<TemplateMetadata> findByTag(String tag);
    Template update(String id, Template template);
    void delete(String id);
    boolean exists(String id);
}
