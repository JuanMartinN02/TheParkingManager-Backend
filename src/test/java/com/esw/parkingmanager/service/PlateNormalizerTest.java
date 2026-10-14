package com.esw.parkingmanager.service;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class PlateNormalizerTest {

    @Test
    void uppercasesLetters() {
        assertThat(PlateNormalizer.normalize("abc123")).isEqualTo("ABC123");
    }

    @Test
    void removesSpacesDashesAndDots() {
        assertThat(PlateNormalizer.normalize("abc-123")).isEqualTo("ABC123");
        assertThat(PlateNormalizer.normalize("ABC 123")).isEqualTo("ABC123");
        assertThat(PlateNormalizer.normalize("A.B.C 1-2-3")).isEqualTo("ABC123");
    }

    @Test
    void nullBecomesEmpty() {
        assertThat(PlateNormalizer.normalize(null)).isEmpty();
    }

    @Test
    void onlySeparatorsBecomesEmpty() {
        assertThat(PlateNormalizer.normalize("- - .")).isEmpty();
    }

    @Test
    void doesNotMergeLetterOAndDigitZero() {
        // Deliberate: O and 0 are different plates. We don't guess.
        assertThat(PlateNormalizer.normalize("O0")).isEqualTo("O0");
    }

    @Test
    void worksTheSameRegardlessOfServerLanguage() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr")); // Turkish
            assertThat(PlateNormalizer.normalize("abi123")).isEqualTo("ABI123");
        } finally {
            Locale.setDefault(original);
        }
    }
}