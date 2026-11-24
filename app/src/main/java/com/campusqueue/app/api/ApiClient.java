package com.campusqueue.app.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.campusqueue.app.db.DatabaseHelper;
import com.campusqueue.app.model.ServiceOffice;
import com.campusqueue.app.model.Ticket;
import com.campusqueue.app.model.TicketStatus;
import com.campusqueue.app.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient implements CampusQueueApi {

    private final DatabaseHelper db;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static ApiClient instance;

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }

    public ApiClient(Context context) {
        this.db = DatabaseHelper.getInstance(context);
    }

    @Override
    public void login(String email, String password, ApiCallback<User> callback) {
        executor.execute(() -> {
            try {
                Thread.sleep(200);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("email", email);
                jsonRequest.put("password", password);

                User user = db.authenticateUser(email, password);
                if (user != null) {
                    if (!user.isActive) {
                        mainHandler.post(() -> callback.onError("Account deactivated. Please contact an administrator."));
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("status", "success");
                    jsonResponse.put("code", 200);

                    JSONObject userData = new JSONObject();
                    userData.put("id", user.id);
                    userData.put("name", user.name);
                    userData.put("email", user.email);
                    userData.put("role", user.role);
                    userData.put("token", "jwt-token-campusqueue-" + user.id);
                    jsonResponse.put("data", userData);

                    mainHandler.post(() -> callback.onSuccess(user));
                } else {
                    mainHandler.post(() -> callback.onError("Invalid email or password"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    @Override
    public void register(User user, ApiCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                Thread.sleep(200);
                if (db.userExists(user.email)) {
                    mainHandler.post(() -> callback.onError("Email already registered"));
                    return;
                }
                boolean success = db.registerUser(user);
                if (success) {
                    mainHandler.post(() -> callback.onSuccess(true));
                } else {
                    mainHandler.post(() -> callback.onError("Registration failed"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    @Override
    public void getServices(ApiCallback<List<ServiceOffice>> callback) {
        executor.execute(() -> {
            try {
                Thread.sleep(150);
                List<ServiceOffice> services = db.getAllServices();

                JSONArray jsonArray = new JSONArray();
                for (ServiceOffice s : services) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", s.getId());
                    obj.put("name", s.getName());
                    obj.put("location", s.getLocation());
                    obj.put("status", s.getStatus());
                    obj.put("avg_service_time_mins", s.getAvgServiceTimeMins());
                    obj.put("waiting_count", s.getWaitingCount());
                    jsonArray.put(obj);
                }

                mainHandler.post(() -> callback.onSuccess(services));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Error fetching services: " + e.getMessage()));
            }
        });
    }

    @Override
    public void createTicket(long serviceId, long userId, ApiCallback<Ticket> callback) {
        executor.execute(() -> {
            try {
                Thread.sleep(200);
                Ticket ticket = db.createTicket(serviceId, userId);
                if (ticket != null) {
                    mainHandler.post(() -> callback.onSuccess(ticket));
                } else {
                    mainHandler.post(() -> callback.onError("Failed to create ticket"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    @Override
    public void getActiveTicket(long userId, ApiCallback<Ticket> callback) {
        executor.execute(() -> {
            try {
                Thread.sleep(150);
                Ticket ticket = db.getUserActiveTicket(userId);
                mainHandler.post(() -> callback.onSuccess(ticket));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Error loading active ticket: " + e.getMessage()));
            }
        });
    }

    @Override
    public void updateTicketStatus(long ticketId, TicketStatus status, String reason, ApiCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                Thread.sleep(150);
                boolean ok = db.updateTicketStatus(ticketId, status, reason);
                if (ok) {
                    mainHandler.post(() -> callback.onSuccess(true));
                } else {
                    mainHandler.post(() -> callback.onError("Status update failed"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Error updating status: " + e.getMessage()));
            }
        });
    }
}
