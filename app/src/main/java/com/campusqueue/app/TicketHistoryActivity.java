package com.campusqueue.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.campusqueue.app.adapter.TicketAdapter;
import com.campusqueue.app.db.DatabaseHelper;
import com.campusqueue.app.db.SessionManager;
import com.campusqueue.app.model.Ticket;

import java.util.List;

public class TicketHistoryActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager sessionManager;
    private ListView lvHistory;
    private TextView tvEmptyHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_history);

        db = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        lvHistory = findViewById(R.id.lvHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

        loadHistory();
    }

    private void loadHistory() {
        long userId = sessionManager.getUserId();
        if (userId == -1) return;

        List<Ticket> history = db.getUserTicketHistory(userId);
        if (history.isEmpty()) {
            if (tvEmptyHistory != null) tvEmptyHistory.setVisibility(View.VISIBLE);
            if (lvHistory != null) lvHistory.setVisibility(View.GONE);
        } else {
            if (tvEmptyHistory != null) tvEmptyHistory.setVisibility(View.GONE);
            if (lvHistory != null) {
                lvHistory.setVisibility(View.VISIBLE);
                TicketAdapter adapter = new TicketAdapter(this, history);
                lvHistory.setAdapter(adapter);
                lvHistory.setOnItemClickListener((parent, view, position, id) -> {
                    Ticket t = history.get(position);
                    Intent intent = new Intent(this, TicketDetailActivity.class);
                    intent.putExtra(TicketDetailActivity.EXTRA_TICKET_ID, t.getId());
                    startActivity(intent);
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }
}
