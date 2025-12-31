package com.zbob.modules.activity;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Turns a request such as "DELETE /api/v1/admin/products/12" into "Deleted products #12". */
public final class ActivityDescriber {

    private static final String PREFIX = "/api/v1/admin/";

    private ActivityDescriber() {
    }

    public static String action(String method, String path) {
        if (path.startsWith(PREFIX + "exports/")) {
            return "EXPORT";
        }
        return switch (method.toUpperCase(Locale.ROOT)) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> method.toUpperCase(Locale.ROOT);
        };
    }

    public static String summary(String method, String path) {
        String rest = path.startsWith(PREFIX) ? path.substring(PREFIX.length()) : path;
        if (rest.startsWith("exports/")) {
            return "Exported " + rest.substring("exports/".length());
        }
        String verb = switch (action(method, path)) {
            case "CREATE" -> "Added";
            case "UPDATE" -> "Changed";
            case "DELETE" -> "Deleted";
            default -> method;
        };
        List<String> parts = Arrays.stream(rest.split("/")).filter(s -> !s.isBlank()).toList();
        StringBuilder text = new StringBuilder(verb);
        for (String part : parts) {
            text.append(part.chars().allMatch(Character::isDigit) ? " #" + part : " " + part.replace('-', ' '));
        }
        return text.toString();
    }
}
