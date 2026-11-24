package com.campusqueue.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.campusqueue.app.api.ApiClient;
import com.campusqueue.app.api.CampusQueueApi;
import com.campusqueue.app.db.DatabaseHelper;
import com.campusqueue.app.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout tilName, tilRegNo, tilEmail, tilPassword, tilConfirm;
    private TextInputEditText etName, etRegNo, etEmail, etPassword, etConfirm;
    private Spinner spFaculty;
    private CheckBox cbTerms;
    private TextView tvFacultyError, tvTermsError;

    private DatabaseHelper db;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = DatabaseHelper.getInstance(this);
        apiClient = ApiClient.getInstance(this);

        tilName = findViewById(R.id.tilName);
        tilRegNo = findViewById(R.id.tilRegNo);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm = findViewById(R.id.tilConfirm);
        etName = findViewById(R.id.etName);
        etRegNo = findViewById(R.id.etRegNo);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        spFaculty = findViewById(R.id.spFaculty);
        cbTerms = findViewById(R.id.cbTerms);
        tvFacultyError = findViewById(R.id.tvFacultyError);
        tvTermsError = findViewById(R.id.tvTermsError);

        View btnRegister = findViewById(R.id.btnRegister);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> attemptRegister());
        }
        View tvLogin = findViewById(R.id.tvLogin);
        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> finish());
        }
    }

    private void showError(TextView tv, int resId) {
        if (tv != null) {
            tv.setText(resId);
            tv.setVisibility(View.VISIBLE);
        }
    }

    private void attemptRegister() {
        if (tilName != null) tilName.setError(null);
        if (tilRegNo != null) tilRegNo.setError(null);
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPassword != null) tilPassword.setError(null);
        if (tilConfirm != null) tilConfirm.setError(null);

        if (tvFacultyError != null) tvFacultyError.setVisibility(View.GONE);
        if (tvTermsError != null) tvTermsError.setVisibility(View.GONE);

        String name = (etName != null && etName.getText() != null) ? etName.getText().toString().trim() : "";
        String regNo = (etRegNo != null && etRegNo.getText() != null) ? etRegNo.getText().toString().trim().toUpperCase() : "";
        String email = (etEmail != null && etEmail.getText() != null) ? etEmail.getText().toString().trim() : "";
        String password = (etPassword != null && etPassword.getText() != null) ? etPassword.getText().toString() : "";
        String confirm = (etConfirm != null && etConfirm.getText() != null) ? etConfirm.getText().toString() : "";

        boolean ok = true;

        if (name.length() == 0) {
            if (tilName != null) tilName.setError(getString(R.string.err_name_required));
            ok = false;
        }

        if (!Validators.isValidRegNo(regNo)) {
            if (tilRegNo != null) tilRegNo.setError(getString(R.string.err_regno_format));
            ok = false;
        }

        if (email.length() == 0) {
            if (tilEmail != null) tilEmail.setError(getString(R.string.err_email_required));
            ok = false;
        } else if (!Validators.isValidEmail(email)) {
            if (tilEmail != null) tilEmail.setError(getString(R.string.err_email_format));
            ok = false;
        } else if (db.userExists(email)) {
            if (tilEmail != null) tilEmail.setError(getString(R.string.err_email_exists));
            ok = false;
        }

        if (spFaculty == null || spFaculty.getSelectedItemPosition() == 0) {
            showError(tvFacultyError, R.string.err_faculty);
            ok = false;
        }

        int pwError = Validators.passwordError(password);
        if (pwError != 0) {
            if (tilPassword != null) tilPassword.setError(getString(pwError));
            ok = false;
        } else if (!password.equals(confirm)) {
            if (tilConfirm != null) tilConfirm.setError(getString(R.string.err_confirm_mismatch));
            ok = false;
        }

        if (cbTerms == null || !cbTerms.isChecked()) {
            showError(tvTermsError, R.string.err_terms);
            ok = false;
        }

        if (!ok) return;

        // All public registrations are strictly assigned the Student role
        String role = getString(R.string.role_student);
        String faculty = spFaculty != null ? spFaculty.getSelectedItem().toString() : "General";

        User newUser = new User(name, regNo, email, password, role, faculty);

        apiClient.register(newUser, new CampusQueueApi.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(RegisterActivity.this, R.string.account_created, Toast.LENGTH_LONG).show();
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                i.putExtra(LoginActivity.EXTRA_EMAIL, email);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                if (tilEmail != null) tilEmail.setError(errorMessage);
            }
        });
    }
}
