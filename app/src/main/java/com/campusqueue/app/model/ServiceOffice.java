package com.campusqueue.app.model;

public class ServiceOffice {
    private long id;
    private String name;
    private String location;
    private String status; // "Open", "Paused", "Closed"
    private int avgServiceTimeMins;
    private String contactPhone;
    private String contactEmail;
    private int waitingCount;

    public ServiceOffice(long id, String name, String location, String status, int avgServiceTimeMins, String contactPhone, String contactEmail) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.status = status;
        this.avgServiceTimeMins = avgServiceTimeMins;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
    }

    public ServiceOffice(String name, String location, String status, int avgServiceTimeMins, String contactPhone, String contactEmail) {
        this(-1, name, location, status, avgServiceTimeMins, contactPhone, contactEmail);
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public int getAvgServiceTimeMins() { return avgServiceTimeMins; }
    public String getContactPhone() { return contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public int getWaitingCount() { return waitingCount; }

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(String status) { this.status = status; }
    public void setAvgServiceTimeMins(int avgServiceTimeMins) { this.avgServiceTimeMins = avgServiceTimeMins; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public void setWaitingCount(int waitingCount) { this.waitingCount = waitingCount; }
}
