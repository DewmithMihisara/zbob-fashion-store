package com.zbob.modules.storage;

import com.zbob.exception.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** Validates an uploaded image and stores it under a generated name. */
@Component
public class ImageUploader {

    private static final Logger log = LoggerFactory.getLogger(ImageUploader.class);

    public record StoredImage(String key, String url) {
    }

    private final StorageService storage;

    public ImageUploader(StorageService storage) {
        this.storage = storage;
    }

    /** The key is generated here, so nothing from the client's filename reaches the storage path. */
    public StoredImage upload(String folder, MultipartFile file) {
        byte[] content = read(file);
        ImageType type = ImageType.detect(Arrays.copyOf(content, Math.min(content.length, 16)))
                .orElseThrow(() -> new BadRequestException("Only JPEG, PNG and WebP images are allowed"));
        String key = folder + "/" + UUID.randomUUID() + "." + type.extension();
        try {
            return new StoredImage(key, storage.store(key, new ByteArrayInputStream(content), content.length, type.contentType()));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store image", e);
        }
    }

    /** Failure only leaves an orphaned file behind, so it is logged rather than raised. */
    public void deleteQuietly(String key) {
        try {
            storage.delete(key);
        } catch (IOException | RuntimeException e) {
            log.warn("Could not delete stored file {}", key, e);
        }
    }

    private static byte[] read(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("The uploaded file could not be read");
        }
    }
}
