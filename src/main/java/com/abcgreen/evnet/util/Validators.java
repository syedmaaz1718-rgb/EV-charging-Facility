package com.abcgreen.evnet.util;

import java.util.regex.Pattern;

/** Input validation (Module I: strings, operators, conditionals). */
public final class Validators {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern REG_NO = Pattern.compile("^[A-Z]{2}[0-9A-Z-]{4,12}$");

    private Validators() {}

    public static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
    }
    public static void requireEmail(String email) {
        requireNonBlank(email, "Email");
        if (!EMAIL.matcher(email.trim()).matches()) throw new IllegalArgumentException("Invalid email format");
    }
    public static void requirePassword(String pw) {
        if (pw == null || pw.length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters");
    }
    public static void requireRegNo(String regNo) {
        requireNonBlank(regNo, "Vehicle number");
        if (!REG_NO.matcher(regNo.trim().toUpperCase()).matches())
            throw new IllegalArgumentException("Invalid vehicle number (e.g. KA05MN2211)");
    }
    public static double requirePositive(double v, String field) {
        if (v <= 0) throw new IllegalArgumentException(field + " must be positive");
        return v;
    }
}
