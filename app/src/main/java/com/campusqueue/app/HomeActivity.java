package com.campusqueue.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.campusqueue.app.db.DatabaseHelper;
import com.campusqueue.app.db.SessionManager;
import com.campusqueue.app.model.ServiceOffice;
import com.campusqueue.app.model.Ticket;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_ROLE = "role";

    private DatabaseHelper db;
    private SessionManager sessionManager;

    private final List<ServiceOffice> allServices = new ArrayList<>();
    private final List<ServiceOffice> shown = new ArrayList<>();
    private ServiceAdapter adapter;

    private TextView tvWelcome, tvRole, tvActiveTicketBanner;
    private Button btnHistory, btnLogout;
    private Spinner spFilter;
    private ListView lvServices;

    private Ticket activeTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        Intent intent = getIntent();
        String name = intent != null ? intent.getStringExtra(EXTRA_NAME) : sessionManager.getUserName();
        String role = intent != null ? intent.getStringExtra(EXTRA_ROLE) : sessionManager.getUserRole();

        if (name == null || name.trim().length() == 0) name = "User";
        if (role == null || role.trim().length() == 0) role = "Student";

        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);
        tvActiveTicketBanner = findViewById(R.id.tvActiveTicketBanner);
        btnHistory = findViewById(R.id.btnHistory);
        btnLogout = findViewById(R.id.btnLogout);
        spFilter = findViewById(R.id.spFilter);
        lvServices = findViewById(R.id.lvServices);

        if (tvWelcome != null) tvWelcome.setText(getString(R.string.home_welcome, name));
        if (tvRole != null) tvRole.setText(getString(R.string.home_role, role));

        adapter = new ServiceAdapter(this, shown);
        if (lvServices != null) {
            lvServices.setAdapter(adapter);
            lvServices.setOnItemClickListener((parent, view, pos, id) -> {
                if (pos >= 0 && pos < shown.size()) {
                    ServiceOffice selected = shown.get(pos);
                    Intent i = new Intent(this, ServiceDetailActivity.class);
                    i.putExtra(ServiceDetailActivity.EXTRA_SERVICE_ID, selected.getId());
                    startActivity(i);
                }
            });
        }

        if (spFilter != null) {
            spFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { applyFilter(pos); }
                @Override public void onNothingSelected(AdapterView<?> p) { }
            });
        }

        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> startActivity(new Intent(this, TicketHistoryActivity.class)));
        }

        if (tvActiveTicketBanner != null) {
            tvActiveTicketBanner.setOnClickListener(v -> {
                if (activeTicket != null) {
                    Intent i = new Intent(this, TicketDetailActivity.class);
                    i.putExtra(TicketDetailActivity.EXTRA_TICKET_ID, activeTicket.getId());
                    startActivity(i);
                }
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                sessionManager.logout();
                Toast.makeText(this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            });
        }

        loadData();
    }

    private void loadData() {
        allServices.clear();
        allServices.addAll(db.getAllServices());

        if (spFilter != null) {
            applyFilter(spFilter.getSelectedItemPosition());
        } else {
            applyFilter(0);
        }

        long userId = sessionManager.getUserId();
        if (userId != -1) {
            activeTicket = db.getUserActiveTicket(userId);
            if (activeTicket != null && tvActiveTicketBanner != null) {
                tvActiveTicketBanner.setVisibility(View.VISIBLE);
                tvActiveTicketBanner.setText("Active Ticket: " + activeTicket.getTicketNumber() + " (" + activeTicket.getStatus().getDisplayName() + ") — Tap to view");
            } else if (tvActiveTicketBanner != null) {
                tvActiveTicketBanner.setVisibility(View.GONE);
            }
        }
    }

    private void applyFilter(int position) {
        shown.clear();
        for (ServiceOffice s : allServices) {
            if (position == 0 || (position == 1 && "Open".equalsIgnoreCase(s.getStatus()))
                    || (position == 2 && "Paused".equalsIgnoreCase(s.getStatus()))) {
                shown.add(s);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
