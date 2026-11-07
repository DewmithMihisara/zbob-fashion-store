package com.zbob.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugsTest {

    @Test
    void lowercasesAndHyphenates() {
        assertThat(Slugs.of("Classic Boxy Tee")).isEqualTo("classic-boxy-tee");
    }

    @Test
    void dropsApostrophes() {
        assertThat(Slugs.of("Men's Wear")).isEqualTo("mens-wear");
    }

    @Test
    void stripsAccentsAndTrimsSeparators() {
        assertThat(Slugs.of("  Café -- Noir! ")).isEqualTo("cafe-noir");
    }
}
