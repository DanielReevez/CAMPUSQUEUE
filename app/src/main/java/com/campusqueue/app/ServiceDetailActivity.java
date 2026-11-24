package com.campusqueue.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.campusqueue.app.api.ApiClient;
import com.campusqueue.app.api.CampusQueueApi;
import com.campusqueue.app.db.DatabaseHelper;
import com.campusqueue.app.db.SessionManager;
import com.campusqueue.app.model.ServiceOffice;
import com.campusqueue.app.model.Ticket;
import com.campusqueue.app.service.NotificationHelper;
import com.campusqueue.app.service.WaitTimeCalculator;

import java.util.List;

public class ServiceDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICE_ID = "extra_service_id";

    private long serviceId = -1;
    private DatabaseHelper db;
    private ApiClient apiClient;
    private SessionManager sessionManager;
    private ServiceOffice serviceOffice;

    private TextView tvServiceName, tvLocation, tvStatus, tvWaitingCount, tvEstWaitTime;
    private Button btnCallOffice, btnEmailOffice, btnJoinQueue;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        serviceId = getIntent().getLongExtra(EXTRA_SERVICE_ID, -1);
        db = DatabaseHelper.getInstance(this);
        apiClient = ApiClient.getInstance(this);
        sessionManager = new SessionManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tvServiceName = findViewById(R.id.tvServiceName);
        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        tvWaitingCount = findViewById(R.id.tvWaitingCount);
        tvEstWaitTime = findViewById(R.id.tvEstWaitTime);
        btnCallOffice = findViewById(R.id.btnCallOffice);
        btnEmailOffice = findViewById(R.id.btnEmailOffice);
        btnJoinQueue = findViewById(R.id.btnJoinQueue);
        progressBar = findViewById(R.id.progressBar);

        loadServiceDetails();

        if (btnCallOffice != null) {
            btnCallOffice.setOnClickListener(v -> {
                if (serviceOffice != null && serviceOffice.getContactPhone() != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + serviceOffice.getContactPhone()));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Phone contact unavailable", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnEmailOffice != null) {
            btnEmailOffice.setOnClickListener(v -> {
                if (serviceOffice != null && serviceOffice.getContactEmail() != null) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + serviceOffice.getContactEmail()));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry: " + serviceOffice.getName());
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Email contact unavailable", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnJoinQueue != null) {
            btnJoinQueue.setOnClickListener(v -> joinQueue());
        }
    }

    private void loadServiceDetails() {
        if (serviceId == -1) return;
        serviceOffice = db.getServiceById(serviceId);
        if (serviceOffice == null) return;

        tvServiceName.setText(serviceOffice.getName());
        tvLocation.setText(serviceOffice.getLocation());
        tvStatus.setText(serviceOffice.getStatus());

        int waiting = db.getWaitingCountForService(serviceId);
        tvWaitingCount.setText(String.valueOf(waiting));

        List<Integer> recentDurations = db.getRecentServiceDurations(serviceId, 10);
        int estMins = WaitTimeCalculator.calculateEstimatedWaitTime(serviceOffice, recentDurations, waiting);
        tvEstWaitTime.setText(estMins + " min");

        boolean isOpen = "Open".equalsIgnoreCase(serviceOffice.getStatus());
        btnJoinQueue.setEnabled(isOpen);
        if (!isOpen) {
            btnJoinQueue.setText("Queue Currently " + serviceOffice.getStatus());
        }
    }

    private void joinQueue() {
        long userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "Please log in to join queue", Toast.LENGTH_SHORT).show();
            return;
        }

        Ticket activeTicket = db.getUserActiveTicket(userId);
        if (activeTicket != null) {
            Toast.makeText(this, "You already have an active ticket (" + activeTicket.getTicketNumber() + ")", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, TicketDetailActivity.class);
            intent.putExtra(TicketDetailActivity.EXTRA_TICKET_ID, activeTicket.getId());
            startActivity(intent);
            finish();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (btnJoinQueue != null) btnJoinQueue.setEnabled(false);

        apiClient.createTicket(serviceId, userId, new CampusQueueApi.ApiCallback<Ticket>() {
            @Override
            public void onSuccess(Ticket ticket) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(ServiceDetailActivity.this, "Joined Queue! Ticket: " + ticket.getTicketNumber(), Toast.LENGTH_LONG).show();

                NotificationHelper.sendNotification(
                        ServiceDetailActivity.this,
                        "Ticket Issued: " + ticket.getTicketNumber(),
                        "You are position #" + ticket.getPosition() + " for " + ticket.getServiceName(),
                        (int) ticket.getId()
                );

                Intent intent = new Intent(ServiceDetailActivity.this, TicketDetailActivity.class);
                intent.putExtra(TicketDetailActivity.EXTRA_TICKET_ID, ticket.getId());
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (btnJoinQueue != null) btnJoinQueue.setEnabled(true);
                Toast.makeText(ServiceDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadServiceDetails();
    }
}
