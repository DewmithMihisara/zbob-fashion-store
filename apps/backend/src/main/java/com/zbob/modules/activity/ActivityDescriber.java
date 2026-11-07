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
        if (path.endsWith("/bulk")) {
            return "UPDATE";
        }
        if (path.endsWith("/password-reset")) {
            return "RESET";
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
        if (rest.endsWith("/bulk")) {
            return "Changed " + rest.substring(0, rest.length() - "/bulk".length()).replace('-', ' ') + " in bulk";
        }
        if (rest.endsWith("/password-reset")) {
            return "Sent a password reset link for " + words(rest.substring(0, rest.length() - "/password-reset".length()));
        }
        String verb = switch (action(method, path)) {
            case "CREATE" -> "Added";
            case "UPDATE" -> "Changed";
            case "DELETE" -> "Deleted";
            default -> method;
        };
        return verb + " " + words(rest);
    }

    private static String words(String rest) {
        List<String> parts = Arrays.stream(rest.split("/")).filter(s -> !s.isBlank()).toList();
        StringBuilder text = new StringBuilder();
        for (String part : parts) {
            if (!text.isEmpty()) {
                text.append(' ');
            }
            text.append(part.chars().allMatch(Character::isDigit) ? "#" + part : part.replace('-', ' '));
        }
        return text.toString();
    }
}
