package com.campusqueue.app.model;

public class User {
    public long id;
    public String name;
    public String regNo;
    public String email;
    public String password;
    public String role;
    public String faculty;
    public boolean isActive = true;
    public boolean mustChangePassword = false;
    public String position = "";

    public User(long id, String name, String regNo, String email, String password, String role, String faculty, boolean isActive, boolean mustChangePassword, String position) {
        this.id = id;
        this.name = name;
        this.regNo = regNo;
        this.email = email;
        this.password = password;
        this.role = role;
        this.faculty = faculty;
        this.isActive = isActive;
        this.mustChangePassword = mustChangePassword;
        this.position = position != null ? position : "";
    }

    public User(long id, String name, String regNo, String email, String password, String role, String faculty) {
        this(id, name, regNo, email, password, role, faculty, true, false, "");
    }

    public User(String name, String regNo, String email, String password, String role, String faculty) {
        this(-1, name, regNo, email, password, role, faculty, true, false, "");
    }

    public User(String name, String email, String password, String role) {
        this(-1, name, "", email, password, role, "General", true, false, "");
    }
}
