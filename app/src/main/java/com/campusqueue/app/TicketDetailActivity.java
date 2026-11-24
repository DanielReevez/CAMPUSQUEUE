package com.campusqueue.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.campusqueue.app.api.ApiClient;
import com.campusqueue.app.api.CampusQueueApi;
import com.campusqueue.app.db.DatabaseHelper;
import com.campusqueue.app.model.ServiceOffice;
import com.campusqueue.app.model.Ticket;
import com.campusqueue.app.model.TicketStatus;
import com.campusqueue.app.service.WaitTimeCalculator;

import java.util.List;

public class TicketDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TICKET_ID = "extra_ticket_id";

    private long ticketId = -1;
    private DatabaseHelper db;
    private ApiClient apiClient;

    private TextView tvServiceName, tvTicketNumber, tvTicketStatus, tvPosition, tvEstWait, tvReturnReason;
    private Button btnCancelTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        ticketId = getIntent().getLongExtra(EXTRA_TICKET_ID, -1);
        db = DatabaseHelper.getInstance(this);
        apiClient = ApiClient.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tvServiceName = findViewById(R.id.tvServiceName);
        tvTicketNumber = findViewById(R.id.tvTicketNumber);
        tvTicketStatus = findViewById(R.id.tvTicketStatus);
        tvPosition = findViewById(R.id.tvPosition);
        tvEstWait = findViewById(R.id.tvEstWait);
        tvReturnReason = findViewById(R.id.tvReturnReason);
        btnCancelTicket = findViewById(R.id.btnCancelTicket);

        loadTicketData();

        if (btnCancelTicket != null) {
            btnCancelTicket.setOnClickListener(v -> confirmCancelTicket());
        }
    }

    private void loadTicketData() {
        if (ticketId == -1) return;
        Ticket ticket = db.getTicketById(ticketId);
        if (ticket == null) return;

        tvServiceName.setText(ticket.getServiceName());
        tvTicketNumber.setText(ticket.getTicketNumber());
        tvTicketStatus.setText(ticket.getStatus().getDisplayName());

        ServiceOffice office = db.getServiceById(ticket.getServiceId());
        int posAhead = db.getPositionInQueue(ticket.getServiceId(), ticket.getId()) - 1;
        if (posAhead < 0) posAhead = 0;

        tvPosition.setText("#" + (posAhead + 1));

        List<Integer> recent = db.getRecentServiceDurations(ticket.getServiceId(), 10);
        int waitMins = WaitTimeCalculator.calculateEstimatedWaitTime(office, recent, posAhead);
        tvEstWait.setText("~" + waitMins + " min");

        if (ticket.getStatus() == TicketStatus.RETURN_REQUIRED && ticket.getReturnReason() != null) {
            tvReturnReason.setVisibility(View.VISIBLE);
            tvReturnReason.setText("Return Reason: " + ticket.getReturnReason());
        } else {
            tvReturnReason.setVisibility(View.GONE);
        }

        boolean canCancel = ticket.getStatus() == TicketStatus.WAITING || ticket.getStatus() == TicketStatus.CALLED;
        btnCancelTicket.setVisibility(canCancel ? View.VISIBLE : View.GONE);
    }

    private void confirmCancelTicket() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Ticket")
                .setMessage("Are you sure you want to cancel your queue ticket?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelTicket())
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelTicket() {
        apiClient.updateTicketStatus(ticketId, TicketStatus.CANCELLED, "Cancelled by student", new CampusQueueApi.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(TicketDetailActivity.this, "Ticket cancelled", Toast.LENGTH_SHORT).show();
                loadTicketData();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(TicketDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTicketData();
    }
}
