package com.hoang.backend.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class TextUtils {

    public static String safe(String value) {
        return value == null ? "" : value;
    }

    public static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public static String safeObject(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    public static String joinName(String firstName, String lastName) {
        String first = safeTrim(firstName);
        String last = safeTrim(lastName);
        if (first.isBlank() && last.isBlank()) return "";
        if (first.isBlank()) return last;
        if (last.isBlank()) return first;
        return first + " " + last;
    }

    public static String asPlain(BigDecimal value) {
        return value == null ? "0" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private TextUtils() {}
}
