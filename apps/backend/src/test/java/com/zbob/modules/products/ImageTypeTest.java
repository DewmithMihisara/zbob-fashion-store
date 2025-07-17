package com.zbob.modules.products;

import static org.assertj.core.api.Assertions.assertThat;

import com.zbob.modules.storage.ImageType;
import org.junit.jupiter.api.Test;

class ImageTypeTest {

    @Test
    void detectsJpeg() {
        assertThat(ImageType.detect(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0})).contains(ImageType.JPEG);
    }

    @Test
    void detectsPng() {
        byte[] head = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0};
        assertThat(ImageType.detect(head)).contains(ImageType.PNG);
    }

    @Test
    void detectsWebp() {
        byte[] head = {'R', 'I', 'F', 'F', 1, 2, 3, 4, 'W', 'E', 'B', 'P'};
        assertThat(ImageType.detect(head)).contains(ImageType.WEBP);
    }

    @Test
    void rejectsOtherContentAndShortFiles() {
        assertThat(ImageType.detect("<svg></svg>".getBytes())).isEmpty();
        assertThat(ImageType.detect(new byte[] {'R', 'I', 'F', 'F'})).isEmpty();
        assertThat(ImageType.detect(new byte[0])).isEmpty();
    }
}
