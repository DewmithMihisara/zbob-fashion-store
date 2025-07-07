package com.zbob.modules.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zbob.config.AppProperties;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalStorageServiceTest {

    @TempDir
    Path dir;

    private LocalStorageService storage;

    @BeforeEach
    void setUp() {
        var settings = new AppProperties.Storage("local", dir.toString(), "http://cdn.test/uploads", null);
        storage = new LocalStorageService(new AppProperties(null, null, null, settings));
    }

    private static ByteArrayInputStream bytes() {
        return new ByteArrayInputStream(new byte[] {1, 2, 3});
    }

    @Test
    void storesFileAndReturnsPublicUrl() throws Exception {
        String url = storage.store("products/1/a.png", bytes(), 3, "image/png");

        assertThat(url).isEqualTo("http://cdn.test/uploads/products/1/a.png");
        assertThat(Files.readAllBytes(dir.resolve("products/1/a.png"))).containsExactly(1, 2, 3);
    }

    @Test
    void deletesFile() throws Exception {
        storage.store("a.png", bytes(), 3, "image/png");

        storage.delete("a.png");

        assertThat(dir.resolve("a.png")).doesNotExist();
    }

    @Test
    void deletingMissingFileIsNotAnError() {
        assertThatCode(() -> storage.delete("missing.png")).doesNotThrowAnyException();
    }

    @Test
    void refusesKeysOutsideTheUploadFolder() {
        assertThatThrownBy(() -> storage.store("../evil.png", bytes(), 3, "image/png"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
