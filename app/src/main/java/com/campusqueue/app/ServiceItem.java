package com.campusqueue.app;

public class ServiceItem {
    public final String name, location, status;
    public final int waiting, estimatedMinutes;
    public ServiceItem(String name, String location, String status, int waiting, int estimatedMinutes) {
        this.name = name; this.location = location; this.status = status;
        this.waiting = waiting; this.estimatedMinutes = estimatedMinutes;
    }
}
