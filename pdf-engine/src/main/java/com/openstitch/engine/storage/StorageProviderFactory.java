package com.openstitch.engine.storage;

import javax.sql.DataSource;

public class StorageProviderFactory {

    public static StorageProvider createFileSystem(String baseDir) {
        return new FileSystemStorageProvider(baseDir);
    }

    public static StorageProvider createDatabase(DataSource dataSource) {
        return new DatabaseStorageProvider(dataSource);
    }
}
