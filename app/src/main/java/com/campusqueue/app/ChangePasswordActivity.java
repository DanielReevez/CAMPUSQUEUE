package com.campusqueue.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.campusqueue.app.db.DatabaseHelper;
import com.campusqueue.app.db.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout tilOldPassword, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etOldPassword, etNewPassword, etConfirmPassword;
    private DatabaseHelper db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        db = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        tilOldPassword = findViewById(R.id.tilOldPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnSubmitChange = findViewById(R.id.btnSubmitChange);

        if (btnSubmitChange != null) {
            btnSubmitChange.setOnClickListener(v -> attemptChangePassword());
        }
    }

    private void attemptChangePassword() {
        if (tilOldPassword != null) tilOldPassword.setError(null);
        if (tilNewPassword != null) tilNewPassword.setError(null);
        if (tilConfirmPassword != null) tilConfirmPassword.setError(null);

        String oldPw = etOldPassword != null && etOldPassword.getText() != null ? etOldPassword.getText().toString() : "";
        String newPw = etNewPassword != null && etNewPassword.getText() != null ? etNewPassword.getText().toString() : "";
        String confirmPw = etConfirmPassword != null && etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        if (oldPw.length() == 0) {
            if (tilOldPassword != null) tilOldPassword.setError("Current password required");
            return;
        }

        String pwErr = Validators.validateStrongPassword(newPw);
        if (pwErr != null) {
            if (tilNewPassword != null) tilNewPassword.setError(pwErr);
            return;
        }

        if (!newPw.equals(confirmPw)) {
            if (tilConfirmPassword != null) tilConfirmPassword.setError("Passwords do not match");
            return;
        }

        long userId = sessionManager.getUserId();
        boolean success = db.updateUserPassword(userId, oldPw, newPw);
        if (success) {
            sessionManager.setMustChangePassword(false);
            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();

            String role = sessionManager.getUserRole();
            String name = sessionManager.getUserName();
            Intent intent;
            if (role != null && (role.equalsIgnoreCase("Service staff") || role.equalsIgnoreCase("Staff"))) {
                intent = new Intent(this, StaffDashboardActivity.class);
            } else if (role != null && (role.equalsIgnoreCase("Administrator") || role.equalsIgnoreCase("Admin"))) {
                intent = new Intent(this, AdminDashboardActivity.class);
            } else {
                intent = new Intent(this, HomeActivity.class);
            }
            intent.putExtra(HomeActivity.EXTRA_NAME, name);
            intent.putExtra(HomeActivity.EXTRA_ROLE, role);
            startActivity(intent);
            finish();
        } else {
            if (tilOldPassword != null) tilOldPassword.setError("Current password is incorrect");
        }
    }
}
