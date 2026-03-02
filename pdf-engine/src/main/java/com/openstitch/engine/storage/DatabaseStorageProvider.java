package com.openstitch.engine.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.openstitch.engine.exception.StorageException;
import com.openstitch.engine.model.Template;
import com.openstitch.engine.model.TemplateMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class DatabaseStorageProvider implements StorageProvider {

    private static final Logger log = LoggerFactory.getLogger(DatabaseStorageProvider.class);

    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public DatabaseStorageProvider(DataSource dataSource) {
        this.dataSource = dataSource;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
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

        String sql = "INSERT INTO templates (id, name, description, author, tags, template_data, version, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, metadata.getId());
            ps.setString(2, metadata.getName());
            ps.setString(3, metadata.getDescription());
            ps.setString(4, metadata.getAuthor());
            ps.setString(5, serializeTags(metadata.getTags()));
            ps.setString(6, objectMapper.writeValueAsString(template));
            ps.setInt(7, metadata.getVersion());
            ps.setTimestamp(8, Timestamp.from(now));
            ps.setTimestamp(9, Timestamp.from(now));

            ps.executeUpdate();
            log.debug("Saved template with id: {}", metadata.getId());
            return template;

        } catch (SQLException | JsonProcessingException e) {
            throw new StorageException("Failed to save template: " + metadata.getId(), e);
        }
    }

    @Override
    public Optional<Template> findById(String id) {
        String sql = "SELECT template_data FROM templates WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("template_data");
                    Template template = objectMapper.readValue(json, Template.class);
                    return Optional.of(template);
                }
                return Optional.empty();
            }

        } catch (Exception e) {
            throw new StorageException("Failed to find template: " + id, e);
        }
    }

    @Override
    public List<TemplateMetadata> findAll() {
        String sql = "SELECT id, name, description, author, tags, version, created_at, updated_at FROM templates ORDER BY updated_at DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<TemplateMetadata> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapResultSetToMetadata(rs));
            }
            return results;

        } catch (SQLException e) {
            throw new StorageException("Failed to list templates", e);
        }
    }

    @Override
    public List<TemplateMetadata> findByTag(String tag) {
        String sql = "SELECT id, name, description, author, tags, version, created_at, updated_at FROM templates ORDER BY updated_at DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<TemplateMetadata> results = new ArrayList<>();
            while (rs.next()) {
                TemplateMetadata metadata = mapResultSetToMetadata(rs);
                if (metadata.getTags() != null && metadata.getTags().contains(tag)) {
                    results.add(metadata);
                }
            }
            return results;

        } catch (SQLException e) {
            throw new StorageException("Failed to find templates by tag: " + tag, e);
        }
    }

    @Override
    public Template update(String id, Template template) {
        Optional<Template> existingOpt = findById(id);
        if (existingOpt.isEmpty()) {
            throw new StorageException("Template not found: " + id);
        }

        Template existing = existingOpt.get();
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

        Instant now = Instant.now();
        existingMeta.setUpdatedAt(now);
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

        String sql = "UPDATE templates SET name = ?, description = ?, author = ?, tags = ?, template_data = ?, "
                + "version = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, existingMeta.getName());
            ps.setString(2, existingMeta.getDescription());
            ps.setString(3, existingMeta.getAuthor());
            ps.setString(4, serializeTags(existingMeta.getTags()));
            ps.setString(5, objectMapper.writeValueAsString(existing));
            ps.setInt(6, existingMeta.getVersion());
            ps.setTimestamp(7, Timestamp.from(now));
            ps.setString(8, id);

            ps.executeUpdate();
            log.debug("Updated template with id: {}", id);
            return existing;

        } catch (SQLException | JsonProcessingException e) {
            throw new StorageException("Failed to update template: " + id, e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM templates WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();
            log.debug("Deleted template with id: {}", id);

        } catch (SQLException e) {
            throw new StorageException("Failed to delete template: " + id, e);
        }
    }

    @Override
    public boolean exists(String id) {
        String sql = "SELECT 1 FROM templates WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new StorageException("Failed to check template existence: " + id, e);
        }
    }

    private TemplateMetadata mapResultSetToMetadata(ResultSet rs) throws SQLException {
        TemplateMetadata metadata = new TemplateMetadata();
        metadata.setId(rs.getString("id"));
        metadata.setName(rs.getString("name"));
        metadata.setDescription(rs.getString("description"));
        metadata.setAuthor(rs.getString("author"));
        metadata.setTags(deserializeTags(rs.getString("tags")));
        metadata.setVersion(rs.getInt("version"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            metadata.setCreatedAt(createdAt.toInstant());
        }
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            metadata.setUpdatedAt(updatedAt.toInstant());
        }

        return metadata;
    }

    private String serializeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            throw new StorageException("Failed to serialize tags", e);
        }
    }

    private List<String> deserializeTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize tags: {}", tagsJson, e);
            return new ArrayList<>();
        }
    }
}
