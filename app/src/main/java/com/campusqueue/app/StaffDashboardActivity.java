package com.campusqueue.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.campusqueue.app.adapter.TicketAdapter;
import com.campusqueue.app.api.ApiClient;
import com.campusqueue.app.api.CampusQueueApi;
import com.campusqueue.app.db.DatabaseHelper;
import com.campusqueue.app.db.SessionManager;
import com.campusqueue.app.model.ServiceOffice;
import com.campusqueue.app.model.Ticket;
import com.campusqueue.app.model.TicketStatus;
import com.campusqueue.app.service.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

public class StaffDashboardActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private ApiClient apiClient;
    private SessionManager sessionManager;

    private TextView tvStaffName, tvAssignedOffice, tvCurrentTicketNum, tvCurrentStudentName, tvCurrentStatus, tvWaitingHeader;
    private Spinner spOffice;
    private Button btnToggleStatus, btnCallNext, btnStartService, btnComplete, btnReturnRequired, btnNoShow, btnLogout;
    private ListView lvWaitingQueue;

    private List<ServiceOffice> serviceOffices = new ArrayList<>();
    private ServiceOffice selectedOffice;
    private Ticket currentActiveTicket;
    private List<Ticket> waitingQueue = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);

        db = DatabaseHelper.getInstance(this);
        apiClient = ApiClient.getInstance(this);
        sessionManager = new SessionManager(this);

        tvStaffName = findViewById(R.id.tvStaffName);
        tvAssignedOffice = findViewById(R.id.tvAssignedOffice);
        tvCurrentTicketNum = findViewById(R.id.tvCurrentTicketNum);
        tvCurrentStudentName = findViewById(R.id.tvCurrentStudentName);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);
        tvWaitingHeader = findViewById(R.id.tvWaitingHeader);
        spOffice = findViewById(R.id.spOffice);
        btnToggleStatus = findViewById(R.id.btnToggleStatus);
        btnCallNext = findViewById(R.id.btnCallNext);
        btnStartService = findViewById(R.id.btnStartService);
        btnComplete = findViewById(R.id.btnComplete);
        btnReturnRequired = findViewById(R.id.btnReturnRequired);
        btnNoShow = findViewById(R.id.btnNoShow);
        btnLogout = findViewById(R.id.btnLogout);
        lvWaitingQueue = findViewById(R.id.lvWaitingQueue);

        tvStaffName.setText(sessionManager.getUserName());

        setupOfficeSpinner();

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                sessionManager.logout();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        if (btnToggleStatus != null) {
            btnToggleStatus.setOnClickListener(v -> toggleOfficeStatus());
        }

        if (btnCallNext != null) {
            btnCallNext.setOnClickListener(v -> callNextStudent());
        }

        if (btnStartService != null) {
            btnStartService.setOnClickListener(v -> updateCurrentTicketStatus(TicketStatus.IN_SERVICE, null));
        }

        if (btnComplete != null) {
            btnComplete.setOnClickListener(v -> updateCurrentTicketStatus(TicketStatus.COMPLETED, null));
        }

        if (btnReturnRequired != null) {
            btnReturnRequired.setOnClickListener(v -> showReturnRequiredDialog());
        }

        if (btnNoShow != null) {
            btnNoShow.setOnClickListener(v -> updateCurrentTicketStatus(TicketStatus.NO_SHOW, null));
        }
    }

    private void setupOfficeSpinner() {
        long staffId = sessionManager.getUserId();
        serviceOffices = db.getAssignedServicesForStaff(staffId);

        if (serviceOffices.isEmpty()) {
            if (tvAssignedOffice != null) {
                tvAssignedOffice.setText("No service office assigned to your account. Contact an administrator.");
            }
            if (btnCallNext != null) btnCallNext.setEnabled(false);
            if (btnStartService != null) btnStartService.setEnabled(false);
            if (btnComplete != null) btnComplete.setEnabled(false);
            if (btnReturnRequired != null) btnReturnRequired.setEnabled(false);
            if (btnNoShow != null) btnNoShow.setEnabled(false);
            if (btnToggleStatus != null) btnToggleStatus.setEnabled(false);
            if (spOffice != null) spOffice.setVisibility(View.GONE);
            return;
        }

        if (spOffice != null) spOffice.setVisibility(View.VISIBLE);

        List<String> names = new ArrayList<>();
        for (ServiceOffice s : serviceOffices) {
            names.add(s.getName() + " (" + s.getLocation() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spOffice.setAdapter(adapter);

        spOffice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOffice = serviceOffices.get(position);
                if (tvAssignedOffice != null) {
                    tvAssignedOffice.setText("Managing: " + selectedOffice.getName());
                }
                refreshQueueData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        selectedOffice = serviceOffices.get(0);
    }

    private boolean checkStaffAuthorization() {
        long staffId = sessionManager.getUserId();
        if (selectedOffice == null || !db.isStaffAssignedToService(staffId, selectedOffice.getId())) {
            Toast.makeText(this, "Unauthorized: You are not assigned to manage this service office.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void refreshQueueData() {
        if (selectedOffice == null) return;

        // Refresh office from DB
        selectedOffice = db.getServiceById(selectedOffice.getId());
        if (selectedOffice == null) return;

        boolean isOpen = "Open".equalsIgnoreCase(selectedOffice.getStatus());
        btnToggleStatus.setText(isOpen ? "Pause Queue" : "Open Queue");

        waitingQueue = db.getWaitingTicketsForService(selectedOffice.getId());

        // Check if there's an active ticket being called or in service
        currentActiveTicket = null;
        List<Ticket> waitingFiltered = new ArrayList<>();

        for (Ticket t : waitingQueue) {
            if (t.getStatus() == TicketStatus.CALLED || t.getStatus() == TicketStatus.IN_SERVICE) {
                if (currentActiveTicket == null) {
                    currentActiveTicket = t;
                }
            } else if (t.getStatus() == TicketStatus.WAITING) {
                waitingFiltered.add(t);
            }
        }

        if (currentActiveTicket != null) {
            tvCurrentTicketNum.setText(currentActiveTicket.getTicketNumber());
            tvCurrentStudentName.setText(currentActiveTicket.getStudentName());
            tvCurrentStatus.setText("Status: " + currentActiveTicket.getStatus().getDisplayName());

            btnCallNext.setEnabled(false);
            btnStartService.setEnabled(currentActiveTicket.getStatus() == TicketStatus.CALLED);
            btnComplete.setEnabled(currentActiveTicket.getStatus() == TicketStatus.IN_SERVICE);
            btnReturnRequired.setEnabled(currentActiveTicket.getStatus() == TicketStatus.IN_SERVICE);
            btnNoShow.setEnabled(currentActiveTicket.getStatus() == TicketStatus.CALLED);
        } else {
            tvCurrentTicketNum.setText("None");
            tvCurrentStudentName.setText("No student currently called");
            tvCurrentStatus.setText("Status: Idle");

            btnCallNext.setEnabled(!waitingFiltered.isEmpty() && isOpen);
            btnStartService.setEnabled(false);
            btnComplete.setEnabled(false);
            btnReturnRequired.setEnabled(false);
            btnNoShow.setEnabled(false);
        }

        tvWaitingHeader.setText("Waiting Students in Queue (" + waitingFiltered.size() + ")");
        TicketAdapter adapter = new TicketAdapter(this, waitingFiltered);
        lvWaitingQueue.setAdapter(adapter);
    }

    private void toggleOfficeStatus() {
        if (!checkStaffAuthorization()) return;
        String newStatus = "Open".equalsIgnoreCase(selectedOffice.getStatus()) ? "Paused" : "Open";
        db.updateServiceStatus(selectedOffice.getId(), newStatus);
        Toast.makeText(this, "Queue status updated to " + newStatus, Toast.LENGTH_SHORT).show();
        refreshQueueData();
    }

    private void callNextStudent() {
        if (!checkStaffAuthorization()) return;
        List<Ticket> waiting = db.getWaitingTicketsForService(selectedOffice.getId());
        Ticket next = null;
        for (Ticket t : waiting) {
            if (t.getStatus() == TicketStatus.WAITING) {
                next = t;
                break;
            }
        }

        if (next == null) {
            Toast.makeText(this, "No waiting students in queue", Toast.LENGTH_SHORT).show();
            return;
        }

        final Ticket targetTicket = next;
        apiClient.updateTicketStatus(targetTicket.getId(), TicketStatus.CALLED, null, new CampusQueueApi.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(StaffDashboardActivity.this, "Called " + targetTicket.getTicketNumber(), Toast.LENGTH_SHORT).show();

                NotificationHelper.sendNotification(
                        StaffDashboardActivity.this,
                        "Your Turn! Ticket " + targetTicket.getTicketNumber(),
                        "Please proceed to " + selectedOffice.getName() + " (" + selectedOffice.getLocation() + ") now!",
                        (int) targetTicket.getId()
                );

                refreshQueueData();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(StaffDashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCurrentTicketStatus(TicketStatus newStatus, String returnReason) {
        if (!checkStaffAuthorization()) return;
        if (currentActiveTicket == null) return;

        apiClient.updateTicketStatus(currentActiveTicket.getId(), newStatus, returnReason, new CampusQueueApi.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(StaffDashboardActivity.this, "Status updated to " + newStatus.getDisplayName(), Toast.LENGTH_SHORT).show();
                refreshQueueData();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(StaffDashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReturnRequiredDialog() {
        if (!checkStaffAuthorization()) return;
        if (currentActiveTicket == null) return;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_return_required, null);
        EditText etReason = view.findViewById(R.id.etReason);

        new AlertDialog.Builder(this)
                .setTitle("Mark Return Required")
                .setView(view)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String reason = etReason != null ? etReason.getText().toString().trim() : "";
                    if (reason.length() == 0) reason = "Documentation or verification required";
                    updateCurrentTicketStatus(TicketStatus.RETURN_REQUIRED, reason);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupOfficeSpinner();
        refreshQueueData();
    }
}
