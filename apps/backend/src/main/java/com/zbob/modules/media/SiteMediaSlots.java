package com.zbob.modules.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Every photo on the storefront that does not belong to a product. Key is the slot, value is its admin label. */
public final class SiteMediaSlots {

    public static final Map<String, String> ALL = build();

    private SiteMediaSlots() {
    }

    private static Map<String, String> build() {
        Map<String, String> slots = new LinkedHashMap<>();
        for (int i = 1; i <= 7; i++) {
            slots.put("home-hero-" + i, "Home hero photo " + i);
        }
        slots.put("home-story", "Home brand story");
        slots.put("shop-hero", "Shop page hero");
        slots.put("about-hero", "About page hero");
        slots.put("about-story", "About page story");
        slots.put("blog-hero", "Blog page hero");
        slots.put("contact-hero", "Contact page hero");
        slots.put("contact-side", "Contact form photo");
        for (String collection : List.of("men", "women", "children")) {
            for (int i = 1; i <= 3; i++) {
                slots.put("collection-" + collection + "-" + i, "Collection: " + collection + " photo " + i);
            }
        }
        for (int i = 1; i <= 6; i++) {
            slots.put("feature-" + i + "-back", "Feature card " + i + " back photo");
            slots.put("feature-" + i + "-front", "Feature card " + i + " front photo");
        }
        for (int i = 1; i <= 7; i++) {
            slots.put("community-" + i, "Community photo " + i);
        }
        return Collections.unmodifiableMap(slots);
    }

    public static List<String> keys() {
        return new ArrayList<>(ALL.keySet());
    }
}
