package com.zbob.modules.storage;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "gcs")
public class GcsConfig {

    /** Uses the credentials of the runtime: the service account on Cloud Run, or GOOGLE_APPLICATION_CREDENTIALS locally. */
    @Bean
    Storage googleCloudStorage() {
        return StorageOptions.getDefaultInstance().getService();
    }
}
