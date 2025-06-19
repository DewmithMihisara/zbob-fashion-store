package com.zbob.common.util;

import java.text.Normalizer;
import java.util.Locale;

public final class Slugs {

    private Slugs() {
    }

    /** Same rules as slugify in @zbob/shared-utils, e.g. "Men's Wear" becomes "mens-wear". */
    public static String of(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .replaceAll("['’]", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
