package com.zbob.modules.storage;

import com.zbob.config.AppProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path root;
    private final String publicBaseUrl;

    public LocalStorageService(AppProperties properties) {
        this.root = Path.of(properties.storage().localDir()).toAbsolutePath().normalize();
        this.publicBaseUrl = properties.storage().publicBaseUrl();
    }

    @Override
    public String store(String key, InputStream content, long size, String contentType) throws IOException {
        Path target = resolve(key);
        Files.createDirectories(target.getParent());
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        return publicBaseUrl + "/" + key;
    }

    @Override
    public void delete(String key) throws IOException {
        Files.deleteIfExists(resolve(key));
    }

    private Path resolve(String key) {
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Storage key escapes the upload folder: " + key);
        }
        return target;
    }
}
