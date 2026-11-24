package com.campusqueue.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Temporary in-memory users for Milestone 1. Replaced by a real database in Milestone 3. */
public class DemoUsers {
    private static final Map<String, User> USERS = new HashMap<>();
    static {
        USERS.put("demo@campusqueue.ac", new User("Daniel", "demo@campusqueue.ac", "DemoStudent123!", "Student"));
    }

    public static boolean exists(String email) {
        if (email == null) return false;
        return USERS.containsKey(email.trim().toLowerCase());
    }

    public static void add(User u) {
        if (u != null && u.email != null) {
            USERS.put(u.email.trim().toLowerCase(), u);
        }
    }

    public static User authenticate(String email, String password) {
        if (email == null || password == null) return null;
        User u = USERS.get(email.trim().toLowerCase());
        return (u != null && Objects.equals(u.password, password)) ? u : null;
    }
}
