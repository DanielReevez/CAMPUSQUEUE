package com.campusqueue.app;

import static org.junit.Assert.*;

import com.campusqueue.app.db.SecurityUtils;

import org.junit.Test;

public class SecurityUtilsTest {

    @Test
    public void testHashPasswordNotNullOrEmpty() {
        String hash = SecurityUtils.hashPassword("DemoStudent123!");
        assertNotNull(hash);
        assertTrue(hash.length() > 0);
        assertNotEquals("DemoStudent123!", hash);
    }

    @Test
    public void testPasswordVerificationMatching() {
        String raw = "MySecurePass123";
        String hash = SecurityUtils.hashPassword(raw);
        assertTrue(SecurityUtils.verifyPassword(raw, hash));
    }

    @Test
    public void testPasswordVerificationNonMatching() {
        String raw = "MySecurePass123";
        String hash = SecurityUtils.hashPassword(raw);
        assertFalse(SecurityUtils.verifyPassword("WrongPassword", hash));
    }

    @Test
    public void testSanitizeInput() {
        String unsafe = "SELECT * FROM users; '--";
        String safe = SecurityUtils.sanitizeInput(unsafe);
        assertFalse(safe.contains("'"));
        assertFalse(safe.contains(";"));
    }
}
