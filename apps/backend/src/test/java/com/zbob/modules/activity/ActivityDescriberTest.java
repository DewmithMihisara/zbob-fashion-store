package com.zbob.modules.activity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActivityDescriberTest {

    @Test
    void describesCommonActions() {
        assertThat(ActivityDescriber.summary("DELETE", "/api/v1/admin/products/12")).isEqualTo("Deleted products #12");
        assertThat(ActivityDescriber.summary("POST", "/api/v1/admin/products/12/images")).isEqualTo("Added products #12 images");
        assertThat(ActivityDescriber.summary("PATCH", "/api/v1/admin/variants/3/stock")).isEqualTo("Changed variants #3 stock");
        assertThat(ActivityDescriber.summary("PUT", "/api/v1/admin/site-media/home-hero-1")).isEqualTo("Changed site media home hero 1");
    }

    @Test
    void describesExports() {
        assertThat(ActivityDescriber.action("GET", "/api/v1/admin/exports/customers.csv")).isEqualTo("EXPORT");
        assertThat(ActivityDescriber.summary("GET", "/api/v1/admin/exports/customers.csv")).isEqualTo("Exported customers.csv");
    }

    @Test
    void namesActions() {
        assertThat(ActivityDescriber.action("POST", "/api/v1/admin/products")).isEqualTo("CREATE");
        assertThat(ActivityDescriber.action("PUT", "/api/v1/admin/products/1")).isEqualTo("UPDATE");
        assertThat(ActivityDescriber.action("DELETE", "/api/v1/admin/products/1")).isEqualTo("DELETE");
    }
}
