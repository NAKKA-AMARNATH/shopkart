package com.amarnath.shopkart.utils;

import java.text.Normalizer;

public class SlugUtils {

    private SlugUtils() {}

    public static String generateSlug(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s+]", "-")
                .replaceAll("-+", "-");
    }
}