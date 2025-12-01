package com.zbob.modules.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.zbob.config.AppProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GcsStorageServiceTest {

    private final Storage client = mock(Storage.class);

    private GcsStorageService service(String bucket) {
        var settings = new AppProperties.Storage("gcs", null, null, bucket);
        return new GcsStorageService(client, new AppProperties(null, null, null, settings));
    }

    private static InputStream bytes() {
        return new ByteArrayInputStream(new byte[] {1, 2, 3});
    }

    @Test
    void uploadsToTheBucketAndReturnsThePublicUrl() throws Exception {
        String url = service("zbob-photos").store("products/1/a.png", bytes(), 3, "image/png");

        assertThat(url).isEqualTo("https://storage.googleapis.com/zbob-photos/products/1/a.png");
        ArgumentCaptor<BlobInfo> blob = ArgumentCaptor.forClass(BlobInfo.class);
        verify(client).createFrom(blob.capture(), any(InputStream.class));
        assertThat(blob.getValue().getBucket()).isEqualTo("zbob-photos");
        assertThat(blob.getValue().getName()).isEqualTo("products/1/a.png");
        assertThat(blob.getValue().getContentType()).isEqualTo("image/png");
    }

    @Test
    void deletesFromTheBucket() throws Exception {
        service("zbob-photos").delete("products/1/a.png");

        verify(client).delete(BlobId.of("zbob-photos", "products/1/a.png"));
    }

    @Test
    void reportsUploadFailures() throws Exception {
        when(client.createFrom(any(BlobInfo.class), any(InputStream.class))).thenThrow(new StorageException(403, "denied"));

        assertThatThrownBy(() -> service("zbob-photos").store("a.png", bytes(), 3, "image/png"))
                .isInstanceOf(IOException.class);
    }

    @Test
    void refusesToStartWithoutABucket() {
        assertThatThrownBy(() -> service("")).isInstanceOf(IllegalStateException.class).hasMessageContaining("STORAGE_BUCKET");
    }
}
