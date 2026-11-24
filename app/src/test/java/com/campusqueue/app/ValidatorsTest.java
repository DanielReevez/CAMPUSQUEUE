package com.campusqueue.app;

import static org.junit.Assert.*;

import org.junit.Test;

public class ValidatorsTest {

    @Test
    public void testValidRegNo() {
        assertTrue(Validators.isValidRegNo("2026/BCS/001"));
        assertTrue(Validators.isValidRegNo("2020/BIT/100/PS"));
    }

    @Test
    public void testInvalidRegNo() {
        assertFalse(Validators.isValidRegNo("INVALID_REG_NO"));
        assertFalse(Validators.isValidRegNo("2026/bcs/001"));
    }

    @Test
    public void testValidEmail() {
        assertTrue(Validators.isValidEmail("demo@campusqueue.ac"));
        assertTrue(Validators.isValidEmail("student@university.edu"));
        assertTrue(Validators.isValidEmail("danieljames@gmail.com"));
        assertTrue(Validators.isValidEmail("admin@campusqueue.ac"));
    }

    @Test
    public void testInvalidEmail() {
        assertFalse(Validators.isValidEmail("not-an-email"));
        assertFalse(Validators.isValidEmail(""));
    }

    @Test
    public void testPasswordStrength() {
        assertEquals(0, Validators.passwordError("DemoStudent123!"));
        assertNotEquals(0, Validators.passwordError("short"));
        assertNotEquals(0, Validators.passwordError("onlyletters"));
        assertNotEquals(0, Validators.passwordError("1234567890"));
    }

    @Test
    public void testValidateStrongPassword() {
        assertNull(Validators.validateStrongPassword("DemoAdmin123!"));
        assertNull(Validators.validateStrongPassword("StaffPass123"));
        assertNotNull(Validators.validateStrongPassword("short"));
        assertNotNull(Validators.validateStrongPassword("lowercaseonly1"));
        assertNotNull(Validators.validateStrongPassword("UPPERCASEONLY1"));
    }
}
