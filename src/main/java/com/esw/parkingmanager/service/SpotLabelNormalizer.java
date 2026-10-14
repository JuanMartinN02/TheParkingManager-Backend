package com.esw.parkingmanager.service;

import java.util.Locale;

public final class SpotLabelNormalizer {

    public SpotLabelNormalizer() {
    }

    public static String normalize(String label) {
        if (label == null) {
            return "";
        }
        return label.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }
}
