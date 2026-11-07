package com.zbob.modules.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.products.repository.ProductImageRepository;
import com.zbob.support.TestData;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestData.class)
@WithMockUser(roles = "STAFF")
class StorageFailureTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @MockitoBean
    private StorageService storage;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private ProductImageRepository images;

    @Test
    void failedUploadReportsAClearErrorAndSavesNoUrl() throws Exception {
        when(storage.store(anyString(), any(), anyLong(), anyString())).thenThrow(new IOException("bucket down"));
        ProductVariant variant = data.variant("Fail Tee", 1000, 1);
        Long productId = variant.getProduct().getId();

        mvc.perform(multipart("/api/v1/admin/products/" + productId + "/images")
                        .file(new MockMultipartFile("file", "a.png", "image/png", PNG)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("STORAGE_UNAVAILABLE"));

        assertThat(images.countByProductId(productId)).isZero();
    }
}
