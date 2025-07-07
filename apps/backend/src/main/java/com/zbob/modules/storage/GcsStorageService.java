package com.zbob.modules.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.zbob.config.AppProperties;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Uses the service account available to the runtime. The bucket must allow public reads of its objects. */
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "gcs")
public class GcsStorageService implements StorageService {

    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final String bucket;

    public GcsStorageService(AppProperties properties) {
        this.bucket = properties.storage().bucket();
    }

    @Override
    public String store(String key, InputStream content, long size, String contentType) throws IOException {
        BlobInfo blob = BlobInfo.newBuilder(BlobId.of(bucket, key)).setContentType(contentType).build();
        storage.createFrom(blob, content);
        return "https://storage.googleapis.com/" + bucket + "/" + key;
    }

    @Override
    public void delete(String key) {
        storage.delete(BlobId.of(bucket, key));
    }
}
