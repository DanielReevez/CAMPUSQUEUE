package com.campusqueue.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.campusqueue.app.adapter.StaffAdapter;
import com.campusqueue.app.db.DatabaseHelper;
import com.campusqueue.app.db.SessionManager;
import com.campusqueue.app.model.ServiceOffice;
import com.campusqueue.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final int MODE_SERVICES = 0;
    private static final int MODE_STAFF = 1;
    private static final int MODE_ADMINS = 2;

    private int currentMode = MODE_SERVICES;

    private DatabaseHelper db;
    private SessionManager sessionManager;

    private TextView tvAdminName, tvStatStudents, tvStatStaff, tvStatAdmins, tvStatOffices, tvStatWaiting, tvSectionTitle;
    private Button btnTabServices, btnTabStaff, btnTabAdmins, btnAddAction, btnLogout;
    private ListView lvAdminItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        tvAdminName = findViewById(R.id.tvAdminName);
        tvStatStudents = findViewById(R.id.tvStatStudents);
        tvStatStaff = findViewById(R.id.tvStatStaff);
        tvStatAdmins = findViewById(R.id.tvStatAdmins);
        tvStatOffices = findViewById(R.id.tvStatOffices);
        tvStatWaiting = findViewById(R.id.tvStatWaiting);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);

        btnTabServices = findViewById(R.id.btnTabServices);
        btnTabStaff = findViewById(R.id.btnTabStaff);
        btnTabAdmins = findViewById(R.id.btnTabAdmins);
        btnAddAction = findViewById(R.id.btnAddAction);
        btnLogout = findViewById(R.id.btnLogout);
        lvAdminItems = findViewById(R.id.lvAdminItems);

        if (tvAdminName != null) tvAdminName.setText(sessionManager.getUserName());

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

        if (btnTabServices != null) {
            btnTabServices.setOnClickListener(v -> switchMode(MODE_SERVICES));
        }
        if (btnTabStaff != null) {
            btnTabStaff.setOnClickListener(v -> switchMode(MODE_STAFF));
        }
        if (btnTabAdmins != null) {
            btnTabAdmins.setOnClickListener(v -> switchMode(MODE_ADMINS));
        }

        if (btnAddAction != null) {
            btnAddAction.setOnClickListener(v -> {
                if (currentMode == MODE_SERVICES) {
                    showAddEditServiceDialog(null);
                } else if (currentMode == MODE_STAFF) {
                    showAddEditStaffDialog(null);
                } else if (currentMode == MODE_ADMINS) {
                    showAddAdminDialog();
                }
            });
        }

        loadAdminStats();
        switchMode(MODE_SERVICES);
    }

    private void loadAdminStats() {
        Bundle stats = db.getAdminStats();
        if (tvStatStudents != null) tvStatStudents.setText(String.valueOf(stats.getInt("totalStudents", 0)));
        if (tvStatStaff != null) tvStatStaff.setText(String.valueOf(stats.getInt("totalStaff", 0)));
        if (tvStatAdmins != null) tvStatAdmins.setText(String.valueOf(stats.getInt("totalAdmins", 0)));
        if (tvStatOffices != null) tvStatOffices.setText(String.valueOf(stats.getInt("openOffices", 0)));
        if (tvStatWaiting != null) tvStatWaiting.setText(String.valueOf(stats.getInt("waitingStudents", 0)));
    }

    private void switchMode(int mode) {
        this.currentMode = mode;
        loadAdminStats();

        if (mode == MODE_SERVICES) {
            if (tvSectionTitle != null) tvSectionTitle.setText("Campus Service Offices");
            if (btnAddAction != null) btnAddAction.setText("+ Add Campus Service Office");
            loadServicesList();
        } else if (mode == MODE_STAFF) {
            if (tvSectionTitle != null) tvSectionTitle.setText("Registered Staff Members");
            if (btnAddAction != null) btnAddAction.setText("+ Add Staff Member");
            loadStaffList();
        } else if (mode == MODE_ADMINS) {
            if (tvSectionTitle != null) tvSectionTitle.setText("System Administrators");
            if (btnAddAction != null) btnAddAction.setText("+ Add Administrator");
            loadAdminList();
        }
    }

    // --- SERVICES SECTION ---

    private void loadServicesList() {
        List<ServiceOffice> services = db.getAllServices();
        ServiceAdapter adapter = new ServiceAdapter(this, services);
        if (lvAdminItems != null) {
            lvAdminItems.setAdapter(adapter);
            lvAdminItems.setOnItemClickListener((parent, view, position, id) -> {
                ServiceOffice selected = services.get(position);
                showServiceOptionsDialog(selected);
            });
        }
    }

    private void showServiceOptionsDialog(ServiceOffice s) {
        String statusAction = "Open".equalsIgnoreCase(s.getStatus()) ? "Pause Office" : "Open Office";
        String[] options = {"Edit Office Details", "Toggle Status (" + statusAction + ")", "Delete Office"};
        new AlertDialog.Builder(this)
                .setTitle(s.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddEditServiceDialog(s);
                    } else if (which == 1) {
                        String newStatus = "Open".equalsIgnoreCase(s.getStatus()) ? "Paused" : "Open";
                        db.updateServiceStatus(s.getId(), newStatus);
                        switchMode(MODE_SERVICES);
                    } else if (which == 2) {
                        confirmDeleteService(s);
                    }
                })
                .show();
    }

    private void showAddEditServiceDialog(ServiceOffice existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_service, null);
        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etName = view.findViewById(R.id.etName);
        EditText etLocation = view.findViewById(R.id.etLocation);
        EditText etAvgMins = view.findViewById(R.id.etAvgMins);
        EditText etPhone = view.findViewById(R.id.etPhone);
        EditText etEmail = view.findViewById(R.id.etEmail);

        if (existing != null) {
            if (tvDialogTitle != null) tvDialogTitle.setText("Edit Campus Service Office");
            if (etName != null) etName.setText(existing.getName());
            if (etLocation != null) etLocation.setText(existing.getLocation());
            if (etAvgMins != null) etAvgMins.setText(String.valueOf(existing.getAvgServiceTimeMins()));
            if (etPhone != null) etPhone.setText(existing.getContactPhone());
            if (etEmail != null) etEmail.setText(existing.getContactEmail());
        }

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Add Service Office" : "Edit Service Office")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName != null ? etName.getText().toString().trim() : "";
                    String loc = etLocation != null ? etLocation.getText().toString().trim() : "";
                    String minsStr = etAvgMins != null ? etAvgMins.getText().toString().trim() : "5";
                    String phone = etPhone != null ? etPhone.getText().toString().trim() : "";
                    String email = etEmail != null ? etEmail.getText().toString().trim() : "";

                    if (name.length() == 0) {
                        Toast.makeText(this, "Office name required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int avgMins = 5;
                    try { avgMins = Integer.parseInt(minsStr); } catch (Exception ignored) {}

                    if (existing == null) {
                        ServiceOffice newOffice = new ServiceOffice(name, loc, "Open", avgMins, phone, email);
                        db.addService(newOffice);
                        Toast.makeText(this, "Office added", Toast.LENGTH_SHORT).show();
                    } else {
                        existing.setName(name);
                        existing.setLocation(loc);
                        existing.setAvgServiceTimeMins(avgMins);
                        existing.setContactPhone(phone);
                        existing.setContactEmail(email);
                        db.updateService(existing);
                        Toast.makeText(this, "Office updated", Toast.LENGTH_SHORT).show();
                    }
                    switchMode(MODE_SERVICES);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteService(ServiceOffice s) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Service Office")
                .setMessage("Are you sure you want to delete " + s.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteService(s.getId());
                    Toast.makeText(this, "Service deleted", Toast.LENGTH_SHORT).show();
                    switchMode(MODE_SERVICES);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- STAFF MANAGEMENT SECTION ---

    private void loadStaffList() {
        List<User> staff = db.getAllStaffMembers();
        StaffAdapter adapter = new StaffAdapter(this, staff);
        if (lvAdminItems != null) {
            lvAdminItems.setAdapter(adapter);
            lvAdminItems.setOnItemClickListener((parent, view, position, id) -> {
                User selected = staff.get(position);
                showStaffOptionsDialog(selected);
            });
        }
    }

    private void showStaffOptionsDialog(User staff) {
        String activeAction = staff.isActive ? "Deactivate Account" : "Activate Account";
        String[] options = {"Edit Staff Details & Assignment", "Reset Password", activeAction};

        new AlertDialog.Builder(this)
                .setTitle(staff.name + " (" + staff.email + ")")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddEditStaffDialog(staff);
                    } else if (which == 1) {
                        promptResetPasswordDialog(staff);
                    } else if (which == 2) {
                        db.toggleUserActiveStatus(staff.id, !staff.isActive);
                        Toast.makeText(this, "Staff account status updated", Toast.LENGTH_SHORT).show();
                        switchMode(MODE_STAFF);
                    }
                })
                .show();
    }

    private void showAddEditStaffDialog(User existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_staff, null);
        TextView tvStaffDialogTitle = view.findViewById(R.id.tvStaffDialogTitle);
        EditText etStaffName = view.findViewById(R.id.etStaffName);
        EditText etStaffEmail = view.findViewById(R.id.etStaffEmail);
        EditText etStaffPosition = view.findViewById(R.id.etStaffPosition);
        EditText etStaffPassword = view.findViewById(R.id.etStaffPassword);
        View tilPassword = view.findViewById(R.id.tilStaffPassword);
        Spinner spStaffService = view.findViewById(R.id.spStaffService);

        List<ServiceOffice> services = db.getAllServices();
        List<String> serviceNames = new ArrayList<>();
        serviceNames.add("-- Select Service Office --");
        for (ServiceOffice s : services) {
            serviceNames.add(s.getName() + " (" + s.getLocation() + ")");
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, serviceNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spStaffService != null) spStaffService.setAdapter(spinnerAdapter);

        if (existing != null) {
            if (tvStaffDialogTitle != null) tvStaffDialogTitle.setText("Edit Staff Member");
            if (etStaffName != null) etStaffName.setText(existing.name);
            if (etStaffEmail != null) etStaffEmail.setText(existing.email);
            if (etStaffPosition != null) etStaffPosition.setText(existing.position);
            if (tilPassword != null) tilPassword.setVisibility(View.GONE);

            List<ServiceOffice> assigned = db.getAssignedServicesForStaff(existing.id);
            if (!assigned.isEmpty() && spStaffService != null) {
                long assignedId = assigned.get(0).getId();
                for (int i = 0; i < services.size(); i++) {
                    if (services.get(i).getId() == assignedId) {
                        spStaffService.setSelection(i + 1);
                        break;
                    }
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Add New Staff Member" : "Edit Staff Member")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etStaffName != null ? etStaffName.getText().toString().trim() : "";
                    String email = etStaffEmail != null ? etStaffEmail.getText().toString().trim() : "";
                    String pos = etStaffPosition != null ? etStaffPosition.getText().toString().trim() : "";
                    String pw = etStaffPassword != null ? etStaffPassword.getText().toString() : "";

                    if (name.length() == 0 || email.length() == 0) {
                        Toast.makeText(this, "Name and email required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!Validators.isValidEmail(email)) {
                        Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long selectedServiceId = -1;
                    if (spStaffService != null && spStaffService.getSelectedItemPosition() > 0) {
                        selectedServiceId = services.get(spStaffService.getSelectedItemPosition() - 1).getId();
                    }

                    if (existing == null) {
                        String pwErr = Validators.validateStrongPassword(pw);
                        if (pwErr != null) {
                            Toast.makeText(this, pwErr, Toast.LENGTH_LONG).show();
                            return;
                        }
                        boolean ok = db.createStaffAccount(name, email, pw, pos, selectedServiceId);
                        if (ok) {
                            Toast.makeText(this, "Staff account created!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed. Email already registered.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        db.updateStaffAccount(existing.id, name, email, pos, selectedServiceId);
                        Toast.makeText(this, "Staff account updated!", Toast.LENGTH_SHORT).show();
                    }
                    switchMode(MODE_STAFF);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- ADMIN MANAGEMENT SECTION ---

    private void loadAdminList() {
        List<User> admins = db.getAllAdminAccounts();
        StaffAdapter adapter = new StaffAdapter(this, admins);
        if (lvAdminItems != null) {
            lvAdminItems.setAdapter(adapter);
            lvAdminItems.setOnItemClickListener((parent, view, position, id) -> {
                User selected = admins.get(position);
                showAdminOptionsDialog(selected);
            });
        }
    }

    private void showAdminOptionsDialog(User admin) {
        String activeAction = admin.isActive ? "Deactivate Account" : "Activate Account";
        String[] options = {"Reset Password", activeAction};

        new AlertDialog.Builder(this)
                .setTitle(admin.name + " (" + admin.email + ")")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        promptResetPasswordDialog(admin);
                    } else if (which == 1) {
                        boolean ok = db.toggleUserActiveStatus(admin.id, !admin.isActive);
                        if (!ok) {
                            Toast.makeText(this, "Cannot deactivate the last active administrator account.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Admin account status updated", Toast.LENGTH_SHORT).show();
                        }
                        switchMode(MODE_ADMINS);
                    }
                })
                .show();
    }

    private void showAddAdminDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_admin, null);
        EditText etAdminName = view.findViewById(R.id.etAdminName);
        EditText etAdminEmail = view.findViewById(R.id.etAdminEmail);
        EditText etAdminPassword = view.findViewById(R.id.etAdminPassword);

        new AlertDialog.Builder(this)
                .setTitle("Add New Administrator")
                .setView(view)
                .setPositiveButton("Create Admin", (dialog, which) -> {
                    String name = etAdminName != null ? etAdminName.getText().toString().trim() : "";
                    String email = etAdminEmail != null ? etAdminEmail.getText().toString().trim() : "";
                    String pw = etAdminPassword != null ? etAdminPassword.getText().toString() : "";

                    if (name.length() == 0 || email.length() == 0) {
                        Toast.makeText(this, "Name and email required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!Validators.isValidEmail(email)) {
                        Toast.makeText(this, "Valid email address required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String pwErr = Validators.validateStrongPassword(pw);
                    if (pwErr != null) {
                        Toast.makeText(this, pwErr, Toast.LENGTH_LONG).show();
                        return;
                    }

                    boolean ok = db.createAdminAccount(name, email, pw);
                    if (ok) {
                        Toast.makeText(this, "Administrator created successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed. Email already registered.", Toast.LENGTH_SHORT).show();
                    }
                    switchMode(MODE_ADMINS);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promptResetPasswordDialog(User targetUser) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_admin, null);
        EditText etName = view.findViewById(R.id.etAdminName);
        EditText etEmail = view.findViewById(R.id.etAdminEmail);
        EditText etPassword = view.findViewById(R.id.etAdminPassword);

        if (etName != null) {
            etName.setText(targetUser.name);
            etName.setEnabled(false);
        }
        if (etEmail != null) {
            etEmail.setText(targetUser.email);
            etEmail.setEnabled(false);
        }

        new AlertDialog.Builder(this)
                .setTitle("Reset Password for " + targetUser.name)
                .setView(view)
                .setPositiveButton("Reset Password", (dialog, which) -> {
                    String newPw = etPassword != null ? etPassword.getText().toString() : "";
                    String pwErr = Validators.validateStrongPassword(newPw);
                    if (pwErr != null) {
                        Toast.makeText(this, pwErr, Toast.LENGTH_LONG).show();
                        return;
                    }
                    db.resetUserPassword(targetUser.id, newPw);
                    Toast.makeText(this, "Password reset! User must change it upon next login.", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchMode(currentMode);
    }
}
