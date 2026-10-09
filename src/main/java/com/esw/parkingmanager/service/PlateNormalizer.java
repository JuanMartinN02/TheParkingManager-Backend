package com.esw.parkingmanager.service;

import java.util.Locale;

public final class PlateNormalizer {
    public PlateNormalizer() {
    }

    public static String normalize(String plate){
        if (plate == null) return "";

        // toUpperCase(Locale.ROOT): without Locale.ROOT,
        // Java uses the server’s language settings.
        // In Turkish, for example, "i" uppercases to "İ" (dotted).
        // Locale.ROOT means “plain, language-neutral rules.”

        //replaceAll("[^A-Z0-9]", ""): removes everything that isn’t a letter or a digit.
        return plate.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

}
