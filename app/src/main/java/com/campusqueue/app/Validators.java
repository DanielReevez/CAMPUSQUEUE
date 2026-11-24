package com.campusqueue.app;

import java.util.regex.Pattern;

public class Validators {
    private static final Pattern REG_NO = Pattern.compile("^\\d{4}/[A-Z]{3}/\\d{3}/[A-Z]{2}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    public static boolean isValidEmail(String s) {
        return s != null && EMAIL_PATTERN.matcher(s.trim()).matches();
    }

    public static boolean isValidRegNo(String s) {
        return s != null && REG_NO.matcher(s.trim()).matches();
    }

    /** Returns a string resource id for the error, or 0 if the password is acceptable. */
    public static int passwordError(String p) {
        if (p == null || p.length() == 0) return R.string.err_password_required;
        if (p.length() < 8) return R.string.err_password_short;
        boolean letter = false;
        boolean digit = false;
        for (char c : p.toCharArray()) {
            if (Character.isLetter(c)) letter = true;
            if (Character.isDigit(c)) digit = true;
        }
        return (letter && digit) ? 0 : R.string.err_password_weak;
    }

    /** Validates strict password policy for Staff & Admin accounts (8+ chars, uppercase, lowercase, digit). */
    public static String validateStrongPassword(String p) {
        if (p == null || p.length() == 0) return "Password is required";
        if (p.length() < 8) return "Password must be at least 8 characters long";
        boolean upper = false, lower = false, digit = false;
        for (char c : p.toCharArray()) {
            if (Character.isUpperCase(c)) upper = true;
            if (Character.isLowerCase(c)) lower = true;
            if (Character.isDigit(c) || !Character.isLetterOrDigit(c)) digit = true;
        }
        if (!upper) return "Password must contain at least one uppercase letter (A-Z)";
        if (!lower) return "Password must contain at least one lowercase letter (a-z)";
        if (!digit) return "Password must contain at least one number or special character";
        return null;
    }
}
