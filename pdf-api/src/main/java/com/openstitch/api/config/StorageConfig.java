package com.openstitch.api.config;

import com.openstitch.engine.storage.StorageProvider;
import com.openstitch.engine.storage.StorageProviderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class StorageConfig {

    @Value("${openstitch.storage.type:filesystem}")
    private String storageType;

    @Value("${openstitch.storage.filesystem.base-dir:./templates}")
    private String baseDir;

    @Bean
    public StorageProvider storageProvider(ObjectProvider<DataSource> dataSourceProvider) {
        if ("database".equalsIgnoreCase(storageType)) {
            DataSource ds = dataSourceProvider.getIfAvailable();
            if (ds == null) {
                throw new IllegalStateException("DataSource required for database storage but not configured");
            }
            return StorageProviderFactory.createDatabase(ds);
        }
        return StorageProviderFactory.createFileSystem(baseDir);
    }
}
