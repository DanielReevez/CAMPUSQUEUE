package com.campusqueue.app;

public class User extends com.campusqueue.app.model.User {
    public User(long id, String name, String regNo, String email, String password, String role, String faculty) {
        super(id, name, regNo, email, password, role, faculty);
    }

    public User(String name, String regNo, String email, String password, String role, String faculty) {
        super(name, regNo, email, password, role, faculty);
    }

    public User(String name, String email, String password, String role) {
        super(name, email, password, role);
    }
}
