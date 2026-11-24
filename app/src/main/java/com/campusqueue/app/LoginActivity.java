package com.campusqueue.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.campusqueue.app.api.ApiClient;
import com.campusqueue.app.api.CampusQueueApi;
import com.campusqueue.app.db.SessionManager;
import com.campusqueue.app.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_EMAIL = "email";
    private static final String TAG = "LIFECYCLE";

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRemember;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Login onCreate (savedState " + (savedInstanceState == null ? "null" : "present") + ")");

        sessionManager = new SessionManager(this);
        apiClient = ApiClient.getInstance(this);

        // Auto login check if session exists
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.mustChangePassword()) {
                startActivity(new Intent(this, ChangePasswordActivity.class));
                finish();
                return;
            }
            navigateToRoleHome(sessionManager.getUserName(), sessionManager.getUserRole());
            return;
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRemember = findViewById(R.id.cbRemember);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgot = findViewById(R.id.tvForgot);

        Intent intent = getIntent();
        String prefill = intent != null ? intent.getStringExtra(EXTRA_EMAIL) : null;
        if (prefill != null && savedInstanceState == null && etEmail != null) {
            etEmail.setText(prefill);
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> attemptLogin());
        }
        if (etPassword != null) {
            etPassword.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            });
        }
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        }
        if (tvForgot != null) {
            tvForgot.setOnClickListener(v ->
                    Toast.makeText(this, R.string.forgot_message, Toast.LENGTH_LONG).show());
        }
    }

    private void attemptLogin() {
        if (tilEmail == null || tilPassword == null || etEmail == null || etPassword == null) return;

        tilEmail.setError(null);
        tilPassword.setError(null);

        CharSequence emailText = etEmail.getText();
        CharSequence passText = etPassword.getText();
        String email = emailText != null ? emailText.toString().trim() : "";
        String password = passText != null ? passText.toString() : "";
        boolean ok = true;

        if (email.length() == 0) {
            tilEmail.setError(getString(R.string.err_email_required));
            ok = false;
        } else if (!Validators.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.err_email_format));
            ok = false;
        }

        int pwError = Validators.passwordError(password);
        if (pwError != 0) {
            tilPassword.setError(getString(pwError));
            ok = false;
        }

        if (!ok) return;

        apiClient.login(email, password, new CampusQueueApi.ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;

                if (!user.isActive) {
                    tilPassword.setError("Account deactivated. Please contact an administrator.");
                    return;
                }

                sessionManager.createSession(user.id, user.email, user.name, user.role, user.regNo, null, user.mustChangePassword);

                Toast.makeText(LoginActivity.this, getString(R.string.welcome_user, user.name), Toast.LENGTH_SHORT).show();

                if (user.mustChangePassword) {
                    startActivity(new Intent(LoginActivity.this, ChangePasswordActivity.class));
                    finish();
                } else {
                    navigateToRoleHome(user.name, user.role);
                }
            }

            @Override
            public void onError(String errorMessage) {
                tilPassword.setError(errorMessage);
            }
        });
    }

    private void navigateToRoleHome(String name, String role) {
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
    }

    // ---- Lifecycle logging (Part E) ----
    @Override protected void onStart() { super.onStart(); Log.d(TAG, "Login onStart"); }
    @Override protected void onRestart() { super.onRestart(); Log.d(TAG, "Login onRestart"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "Login onResume"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "Login onPause"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "Login onStop"); }
    @Override protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Login onDestroy (isChangingConfigurations=" + isChangingConfigurations() + ")");
    }
}
