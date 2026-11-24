package com.campusqueue.app.api;

import com.campusqueue.app.model.ServiceOffice;
import com.campusqueue.app.model.Ticket;
import com.campusqueue.app.model.TicketStatus;
import com.campusqueue.app.model.User;

import java.util.List;

public interface CampusQueueApi {

    interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    void login(String email, String password, ApiCallback<User> callback);
    void register(User user, ApiCallback<Boolean> callback);
    void getServices(ApiCallback<List<ServiceOffice>> callback);
    void createTicket(long serviceId, long userId, ApiCallback<Ticket> callback);
    void getActiveTicket(long userId, ApiCallback<Ticket> callback);
    void updateTicketStatus(long ticketId, TicketStatus status, String reason, ApiCallback<Boolean> callback);
}
