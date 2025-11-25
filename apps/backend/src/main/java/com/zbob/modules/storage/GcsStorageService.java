package com.zbob.modules.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.zbob.config.AppProperties;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Saves uploads in a Google Cloud Storage bucket. The returned URL is what gets saved in the database. */
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "gcs")
public class GcsStorageService implements StorageService {

    private final Storage storage;
    private final String bucket;

    public GcsStorageService(Storage storage, AppProperties properties) {
        this.storage = storage;
        this.bucket = properties.storage().bucket();
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalStateException("STORAGE_BUCKET must be set when STORAGE_PROVIDER is gcs");
        }
    }

    @Override
    public String store(String key, InputStream content, long size, String contentType) throws IOException {
        BlobInfo blob = BlobInfo.newBuilder(BlobId.of(bucket, key)).setContentType(contentType).build();
        try {
            storage.createFrom(blob, content);
        } catch (StorageException e) {
            throw new IOException("Could not upload " + key + " to bucket " + bucket, e);
        }
        return "https://storage.googleapis.com/" + bucket + "/" + key;
    }

    @Override
    public void delete(String key) throws IOException {
        try {
            storage.delete(BlobId.of(bucket, key));
        } catch (StorageException e) {
            throw new IOException("Could not delete " + key + " from bucket " + bucket, e);
        }
    }
}
