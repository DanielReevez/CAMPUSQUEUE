# CampusQueue — Campus Service Queue Management System

**CSC3109 Mobile Applications Development · BCS III · 2026/2027**
Student: CampusQueue Administrator · Reg. No: 2024/BCS/035/PS · Lecturer: Nakato Constance Nakimuli

CampusQueue is a native Android application built in Java and XML designed to eliminate crowding at campus service points (ICT Help Desk, Bursar's Office, Registration, Clinic, Library, Student Affairs) through a virtual queue system.

---

## 1. System Upgrades & Architecture

### Student-Only Public Registration
- Public registration (`RegisterActivity`) creates **Student** accounts exclusively.
- All newly registered public accounts automatically receive the `Student` role.

### Administrator Dashboard & Management (`AdminDashboardActivity`)
- **System Overview**: Live database metrics displaying total students, staff, admins, open/closed offices, and active waiting students.
- **Staff Management**: Administrators can create staff accounts with normal email addresses (e.g. `danieljames@gmail.com`), staff position/title, initial password, and service office assignments.
- **Admin Management**: Administrators can create extra admin accounts, reset passwords, and toggle active status. Includes a safeguard preventing deactivation/deletion of the last active administrator.
- **Service & Staff Assignment Management**: Add, edit, pause, and delete campus service offices and manage staff-to-service assignments (`staff_service_assignments`).

### Restricted Staff Dashboard (`StaffDashboardActivity`)
- Staff members can view and manage **ONLY** service offices assigned to them by an administrator.
- Enforced at both UI layer (assigned office Spinner) and backend DB layer (`db.isStaffAssignedToService`).

### Password Security & Mandatory Password Updates (`ChangePasswordActivity`)
- **Strong Password Policy (`Validators.validateStrongPassword`)**: Enforces minimum 8 characters, uppercase, lowercase, and digits/symbols for Staff & Admin accounts.
- **`ChangePasswordActivity`**: Prompts users to update initial default or reset passwords upon first login.

---

## 2. Local Demo Accounts

The app seeds local demonstration accounts into its on-device SQLite database. These are test-only accounts and must never be reused for real university systems. Their demo credentials are defined in `DatabaseHelper.java`.

## 3. Database Schema

Managed via `DatabaseHelper.java` (`campus_queue.db` v2):

1. **`users`**: `id`, `name`, `reg_no`, `email`, `password_hash`, `role`, `faculty`, `created_at`, `is_active`, `must_change_password`, `position`
2. **`services`**: `id`, `name`, `location`, `status`, `avg_service_time_mins`, `contact_phone`, `contact_email`
3. **`tickets`**: `id`, `ticket_number`, `service_id`, `user_id`, `status`, `position`, `return_reason`, `created_at`, `called_at`, `served_at`, `completed_at`
4. **`service_records`**: `id`, `ticket_id`, `service_id`, `duration_minutes`, `completed_at`
5. **`staff_service_assignments`**: `id`, `staff_id`, `service_id`, `created_at`

---

## 4. REST API Documentation

Implemented by `ApiClient.java`:

| HTTP Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Authenticate user credentials, checks `is_active` status & `must_change_password` flag |
| `POST` | `/api/v1/auth/register` | Register new student account |
| `GET` | `/api/v1/services` | Get list of campus service offices and waiting counts |
| `POST` | `/api/v1/tickets` | Join queue and generate new ticket |
| `GET` | `/api/v1/tickets/my-active` | Retrieve student's current active ticket |
| `PUT` | `/api/v1/tickets/{id}/status` | Update ticket status (`CALLED`, `IN_SERVICE`, `COMPLETED`, `RETURN_REQUIRED`, `NO_SHOW`, `CANCELLED`) |

---

## 5. Security & Risk Matrix

| Risk / Area | Defense Implemented in CampusQueue |
|---|---|
| Plaintext password leak | SHA-256 with a fixed application salt in `SecurityUtils.hashPassword()` (demo implementation only; use a password KDF such as Argon2id, bcrypt, or PBKDF2 with per-user salts for production) |
| SQL Injection | Parameterized queries (`?` bindings) in `DatabaseHelper` |
| Unauthorized feature access | Role-based access control (RBAC) in `LoginActivity`, `SessionManager`, and DB assignment verification |
| Admin Account Lockout | Safeguard preventing deactivation or deletion of the last active administrator |
| Malformed input / XSS | Input sanitization and format validation in `Validators` |

---

## 6. How to Build & Test

### Automated Unit Tests
Run unit tests in Android Studio or terminal:
```powershell
./gradlew testDebugUnitTest
```
> Expected output: `17 passed, 0 skipped, 0 failed`

### Assemble Debug APK
```powershell
./gradlew assembleDebug
```
> Outputs debug APK to `app/build/outputs/apk/debug/app-debug.apk`
