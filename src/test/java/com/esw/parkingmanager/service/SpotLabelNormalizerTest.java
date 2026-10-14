package com.esw.parkingmanager.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpotLabelNormalizerTest {

    @Test
    void keepsPlainNumbers() {
        assertThat(SpotLabelNormalizer.normalize("480")).isEqualTo("480");
    }

    @Test
    void handlesLetterPrefixesAndSuffixes() {
        assertThat(SpotLabelNormalizer.normalize("a-12")).isEqualTo("A12");
        assertThat(SpotLabelNormalizer.normalize("101a")).isEqualTo("101A");
        assertThat(SpotLabelNormalizer.normalize("VIP 1")).isEqualTo("VIP1");
    }

    @Test
    void keepsLeadingZeros() {
        // "P-001" and "P-1" are DIFFERENT spots; zero-padding is meaningful
        assertThat(SpotLabelNormalizer.normalize("P-001")).isEqualTo("P001");
    }

    @Test
    void nullOrOnlySeparatorsBecomesEmpty() {
        assertThat(SpotLabelNormalizer.normalize(null)).isEmpty();
        assertThat(SpotLabelNormalizer.normalize(" - ")).isEmpty();
    }
}