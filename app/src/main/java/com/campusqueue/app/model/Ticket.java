package com.campusqueue.app.model;

public class Ticket {
    private long id;
    private String ticketNumber;
    private long serviceId;
    private String serviceName;
    private long userId;
    private String studentName;
    private TicketStatus status;
    private int position;
    private String returnReason;
    private long createdAt;
    private long calledAt;
    private long servedAt;
    private long completedAt;

    public Ticket(long id, String ticketNumber, long serviceId, String serviceName,
                  long userId, String studentName, TicketStatus status, int position,
                  String returnReason, long createdAt, long calledAt, long servedAt, long completedAt) {
        this.id = id;
        this.ticketNumber = ticketNumber;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.userId = userId;
        this.studentName = studentName;
        this.status = status;
        this.position = position;
        this.returnReason = returnReason;
        this.createdAt = createdAt;
        this.calledAt = calledAt;
        this.servedAt = servedAt;
        this.completedAt = completedAt;
    }

    public long getId() { return id; }
    public String getTicketNumber() { return ticketNumber; }
    public long getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public long getUserId() { return userId; }
    public String getStudentName() { return studentName; }
    public TicketStatus getStatus() { return status; }
    public int getPosition() { return position; }
    public String getReturnReason() { return returnReason; }
    public long getCreatedAt() { return createdAt; }
    public long getCalledAt() { return calledAt; }
    public long getServedAt() { return servedAt; }
    public long getCompletedAt() { return completedAt; }

    public void setPosition(int position) { this.position = position; }
    public void setStatus(TicketStatus status) { this.status = status; }
}
