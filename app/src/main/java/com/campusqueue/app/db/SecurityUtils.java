package com.campusqueue.app.db;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    private static final String SALT = "CampusDemoStudent123!_Salt$";

    /**
     * Hashes a plain password using SHA-256 with salt.
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String salted = SALT + password;
            byte[] hash = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return String.valueOf(password.hashCode());
        }
    }

    /**
     * Verifies if a raw password matches a stored hash.
     */
    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        String computed = hashPassword(rawPassword);
        return computed.equalsIgnoreCase(storedHash);
    }

    /**
     * Basic input sanitization to prevent XSS or SQL characters.
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("['\";]", "");
    }
}
