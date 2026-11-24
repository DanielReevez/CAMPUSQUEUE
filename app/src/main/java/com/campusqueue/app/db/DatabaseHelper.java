package com.campusqueue.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import com.campusqueue.app.model.ServiceOffice;
import com.campusqueue.app.model.Ticket;
import com.campusqueue.app.model.TicketStatus;
import com.campusqueue.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "campus_queue.db";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_SERVICES = "services";
    public static final String TABLE_TICKETS = "tickets";
    public static final String TABLE_SERVICE_RECORDS = "service_records";
    public static final String TABLE_STAFF_ASSIGNMENTS = "staff_service_assignments";

    // Common Column
    public static final String KEY_ID = "id";

    // USERS Columns
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_REG_NO = "reg_no";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_PASSWORD_HASH = "password_hash";
    public static final String KEY_USER_ROLE = "role";
    public static final String KEY_USER_FACULTY = "faculty";
    public static final String KEY_USER_CREATED_AT = "created_at";
    public static final String KEY_USER_IS_ACTIVE = "is_active";
    public static final String KEY_USER_MUST_CHANGE_PASSWORD = "must_change_password";
    public static final String KEY_USER_POSITION = "position";

    // SERVICES Columns
    public static final String KEY_SERVICE_NAME = "name";
    public static final String KEY_SERVICE_LOCATION = "location";
    public static final String KEY_SERVICE_STATUS = "status";
    public static final String KEY_SERVICE_AVG_MINS = "avg_service_time_mins";
    public static final String KEY_SERVICE_PHONE = "contact_phone";
    public static final String KEY_SERVICE_EMAIL = "contact_email";

    // TICKETS Columns
    public static final String KEY_TICKET_NUMBER = "ticket_number";
    public static final String KEY_TICKET_SERVICE_ID = "service_id";
    public static final String KEY_TICKET_USER_ID = "user_id";
    public static final String KEY_TICKET_STATUS = "status";
    public static final String KEY_TICKET_POSITION = "position";
    public static final String KEY_TICKET_RETURN_REASON = "return_reason";
    public static final String KEY_TICKET_CREATED_AT = "created_at";
    public static final String KEY_TICKET_CALLED_AT = "called_at";
    public static final String KEY_TICKET_SERVED_AT = "served_at";
    public static final String KEY_TICKET_COMPLETED_AT = "completed_at";

    // SERVICE RECORDS Columns
    public static final String KEY_RECORD_TICKET_ID = "ticket_id";
    public static final String KEY_RECORD_SERVICE_ID = "service_id";
    public static final String KEY_RECORD_DURATION = "duration_minutes";
    public static final String KEY_RECORD_COMPLETED_AT = "completed_at";

    // STAFF ASSIGNMENTS Columns
    public static final String KEY_ASSIGNMENT_STAFF_ID = "staff_id";
    public static final String KEY_ASSIGNMENT_SERVICE_ID = "service_id";
    public static final String KEY_ASSIGNMENT_CREATED_AT = "created_at";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_NAME + " TEXT,"
                + KEY_USER_REG_NO + " TEXT,"
                + KEY_USER_EMAIL + " TEXT UNIQUE,"
                + KEY_USER_PASSWORD_HASH + " TEXT,"
                + KEY_USER_ROLE + " TEXT,"
                + KEY_USER_FACULTY + " TEXT,"
                + KEY_USER_CREATED_AT + " INTEGER,"
                + KEY_USER_IS_ACTIVE + " INTEGER DEFAULT 1,"
                + KEY_USER_MUST_CHANGE_PASSWORD + " INTEGER DEFAULT 0,"
                + KEY_USER_POSITION + " TEXT DEFAULT ''" + ")";

        String CREATE_SERVICES_TABLE = "CREATE TABLE " + TABLE_SERVICES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SERVICE_NAME + " TEXT,"
                + KEY_SERVICE_LOCATION + " TEXT,"
                + KEY_SERVICE_STATUS + " TEXT,"
                + KEY_SERVICE_AVG_MINS + " INTEGER,"
                + KEY_SERVICE_PHONE + " TEXT,"
                + KEY_SERVICE_EMAIL + " TEXT" + ")";

        String CREATE_TICKETS_TABLE = "CREATE TABLE " + TABLE_TICKETS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TICKET_NUMBER + " TEXT,"
                + KEY_TICKET_SERVICE_ID + " INTEGER,"
                + KEY_TICKET_USER_ID + " INTEGER,"
                + KEY_TICKET_STATUS + " TEXT,"
                + KEY_TICKET_POSITION + " INTEGER,"
                + KEY_TICKET_RETURN_REASON + " TEXT,"
                + KEY_TICKET_CREATED_AT + " INTEGER,"
                + KEY_TICKET_CALLED_AT + " INTEGER,"
                + KEY_TICKET_SERVED_AT + " INTEGER,"
                + KEY_TICKET_COMPLETED_AT + " INTEGER" + ")";

        String CREATE_SERVICE_RECORDS_TABLE = "CREATE TABLE " + TABLE_SERVICE_RECORDS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_RECORD_TICKET_ID + " INTEGER,"
                + KEY_RECORD_SERVICE_ID + " INTEGER,"
                + KEY_RECORD_DURATION + " INTEGER,"
                + KEY_RECORD_COMPLETED_AT + " INTEGER" + ")";

        String CREATE_STAFF_ASSIGNMENTS_TABLE = "CREATE TABLE " + TABLE_STAFF_ASSIGNMENTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_ASSIGNMENT_STAFF_ID + " INTEGER,"
                + KEY_ASSIGNMENT_SERVICE_ID + " INTEGER,"
                + KEY_ASSIGNMENT_CREATED_AT + " INTEGER" + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_SERVICES_TABLE);
        db.execSQL(CREATE_TICKETS_TABLE);
        db.execSQL(CREATE_SERVICE_RECORDS_TABLE);
        db.execSQL(CREATE_STAFF_ASSIGNMENTS_TABLE);

        seedDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_USER_IS_ACTIVE + " INTEGER DEFAULT 1");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_USER_MUST_CHANGE_PASSWORD + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_USER_POSITION + " TEXT DEFAULT ''");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error adding columns on upgrade: " + e.getMessage());
            }

            String CREATE_STAFF_ASSIGNMENTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_STAFF_ASSIGNMENTS + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_ASSIGNMENT_STAFF_ID + " INTEGER,"
                    + KEY_ASSIGNMENT_SERVICE_ID + " INTEGER,"
                    + KEY_ASSIGNMENT_CREATED_AT + " INTEGER" + ")";
            db.execSQL(CREATE_STAFF_ASSIGNMENTS_TABLE);

            seedDefaultAdminAccount(db);
        }
    }

    private void seedDefaultData(SQLiteDatabase db) {
        long now = System.currentTimeMillis();

        // Seed Default Users
        insertDefaultUser(db, "Demo Student", "2026/BCS/001", "demo@campusqueue.ac", "DemoStudent123!", "Student", "Computing and Informatics", now, 1, 0, "");
        insertDefaultUser(db, "Demo Staff", "2020/STA/001/PS", "staff@campusqueue.ac", "DemoStaff123!", "Service staff", "Administration", now, 1, 0, "Senior Helpdesk Assistant");

        // Seed Default Admin Account
        seedDefaultAdminAccount(db);

        // Seed Default Services
        insertDefaultService(db, "ICT help desk", "Computing building, ground floor", "Open", 5, "+256 700 111222", "ict@campus.ac.ug");
        insertDefaultService(db, "Bursar's office", "Administration block", "Open", 8, "+256 700 333444", "bursar@campus.ac.ug");
        insertDefaultService(db, "Registration office", "Administration block", "Paused", 6, "+256 700 555666", "registrar@campus.ac.ug");
        insertDefaultService(db, "University clinic", "Health centre", "Open", 10, "+256 700 777888", "clinic@campus.ac.ug");
        insertDefaultService(db, "Library desk", "Main library", "Open", 3, "+256 700 999000", "library@campus.ac.ug");
        insertDefaultService(db, "Student affairs", "Guild block", "Open", 5, "+256 700 123456", "affairs@campus.ac.ug");

        // Assign default staff Demo Staff (user_id = 2) to ICT Help Desk (service_id = 1)
        ContentValues cvAssign = new ContentValues();
        cvAssign.put(KEY_ASSIGNMENT_STAFF_ID, 2);
        cvAssign.put(KEY_ASSIGNMENT_SERVICE_ID, 1);
        cvAssign.put(KEY_ASSIGNMENT_CREATED_AT, now);
        db.insert(TABLE_STAFF_ASSIGNMENTS, null, cvAssign);
    }

    private void seedDefaultAdminAccount(SQLiteDatabase db) {
        String adminEmail = "admin@campusqueue.ac";
        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_ID}, KEY_USER_EMAIL + "=?", new String[]{adminEmail}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return;
        }
        if (cursor != null) cursor.close();

        long now = System.currentTimeMillis();
        ContentValues cv = new ContentValues();
        cv.put(KEY_USER_NAME, "CampusQueue Administrator");
        cv.put(KEY_USER_REG_NO, "2026/BCS/001");
        cv.put(KEY_USER_EMAIL, adminEmail);
        cv.put(KEY_USER_PASSWORD_HASH, SecurityUtils.hashPassword("DemoAdmin123!"));
        cv.put(KEY_USER_ROLE, "ADMIN");
        cv.put(KEY_USER_FACULTY, "Administration");
        cv.put(KEY_USER_CREATED_AT, now);
        cv.put(KEY_USER_IS_ACTIVE, 1);
        cv.put(KEY_USER_MUST_CHANGE_PASSWORD, 1);
        cv.put(KEY_USER_POSITION, "System Administrator");
        db.insert(TABLE_USERS, null, cv);
    }

    private void insertDefaultUser(SQLiteDatabase db, String name, String regNo, String email, String password, String role, String faculty, long now, int isActive, int mustChangePw, String position) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_USER_NAME, name);
        cv.put(KEY_USER_REG_NO, regNo);
        cv.put(KEY_USER_EMAIL, email.toLowerCase().trim());
        cv.put(KEY_USER_PASSWORD_HASH, SecurityUtils.hashPassword(password));
        cv.put(KEY_USER_ROLE, role);
        cv.put(KEY_USER_FACULTY, faculty);
        cv.put(KEY_USER_CREATED_AT, now);
        cv.put(KEY_USER_IS_ACTIVE, isActive);
        cv.put(KEY_USER_MUST_CHANGE_PASSWORD, mustChangePw);
        cv.put(KEY_USER_POSITION, position);
        db.insert(TABLE_USERS, null, cv);
    }

    private void insertDefaultService(SQLiteDatabase db, String name, String location, String status, int avgMins, String phone, String email) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_SERVICE_NAME, name);
        cv.put(KEY_SERVICE_LOCATION, location);
        cv.put(KEY_SERVICE_STATUS, status);
        cv.put(KEY_SERVICE_AVG_MINS, avgMins);
        cv.put(KEY_SERVICE_PHONE, phone);
        cv.put(KEY_SERVICE_EMAIL, email);
        db.insert(TABLE_SERVICES, null, cv);
    }

    // --- USER CRUD ---

    public boolean registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_USER_NAME, user.name);
        cv.put(KEY_USER_REG_NO, user.regNo);
        cv.put(KEY_USER_EMAIL, user.email.toLowerCase().trim());
        cv.put(KEY_USER_PASSWORD_HASH, SecurityUtils.hashPassword(user.password));
        cv.put(KEY_USER_ROLE, user.role != null ? user.role : "Student");
        cv.put(KEY_USER_FACULTY, user.faculty);
        cv.put(KEY_USER_CREATED_AT, System.currentTimeMillis());
        cv.put(KEY_USER_IS_ACTIVE, user.isActive ? 1 : 0);
        cv.put(KEY_USER_MUST_CHANGE_PASSWORD, user.mustChangePassword ? 1 : 0);
        cv.put(KEY_USER_POSITION, user.position != null ? user.position : "");

        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashed = SecurityUtils.hashPassword(password);
        Cursor cursor = db.query(TABLE_USERS, null,
                KEY_USER_EMAIL + "=? AND " + KEY_USER_PASSWORD_HASH + "=?",
                new String[]{email.toLowerCase().trim(), hashed},
                null, null, null);

        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = extractUserFromCursor(cursor);
            }
            cursor.close();
        }
        return user;
    }

    public boolean userExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_ID},
                KEY_USER_EMAIL + "=?",
                new String[]{email.toLowerCase().trim()},
                null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                KEY_USER_EMAIL + "=?",
                new String[]{email.toLowerCase().trim()},
                null, null, null);
        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = extractUserFromCursor(cursor);
            }
            cursor.close();
        }
        return user;
    }

    public User getUserById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = extractUserFromCursor(cursor);
            }
            cursor.close();
        }
        return user;
    }

    private User extractUserFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_NAME));
        String regNo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_REG_NO));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL));
        String hash = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD_HASH));
        String role = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE));
        String faculty = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_FACULTY));
        int isActive = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_IS_ACTIVE));
        int mustChangePw = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_MUST_CHANGE_PASSWORD));
        String position = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_POSITION));
        return new User(id, name, regNo, email, hash, role, faculty, isActive == 1, mustChangePw == 1, position);
    }

    // --- ADMIN & STAFF MANAGEMENT CRUD ---

    public Bundle getAdminStats() {
        Bundle b = new Bundle();
        SQLiteDatabase db = this.getReadableDatabase();

        b.putInt("totalStudents", getCountByQuery(db, "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE role='Student' OR role='STUDENT'"));
        b.putInt("totalStaff", getCountByQuery(db, "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE role LIKE '%Staff%' OR role='STAFF'"));
        b.putInt("totalAdmins", getCountByQuery(db, "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE role LIKE '%Admin%' OR role='ADMIN'"));
        b.putInt("totalServices", getCountByQuery(db, "SELECT COUNT(*) FROM " + TABLE_SERVICES));
        b.putInt("openOffices", getCountByQuery(db, "SELECT COUNT(*) FROM " + TABLE_SERVICES + " WHERE status='Open'"));
        b.putInt("closedOffices", getCountByQuery(db, "SELECT COUNT(*) FROM " + TABLE_SERVICES + " WHERE status!='Open'"));
        b.putInt("waitingStudents", getCountByQuery(db, "SELECT COUNT(*) FROM " + TABLE_TICKETS + " WHERE status='WAITING'"));
        b.putInt("inServiceStudents", getCountByQuery(db, "SELECT COUNT(*) FROM " + TABLE_TICKETS + " WHERE status='IN_SERVICE' OR status='CALLED'"));

        return b;
    }

    private int getCountByQuery(SQLiteDatabase db, String query) {
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public List<User> getAllStaffMembers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                "role LIKE '%Staff%' OR role='STAFF'", null, null, null, KEY_USER_NAME + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(extractUserFromCursor(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public boolean createStaffAccount(String name, String email, String password, String position, long serviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (userExists(email)) return false;

        ContentValues cv = new ContentValues();
        cv.put(KEY_USER_NAME, name);
        cv.put(KEY_USER_REG_NO, "");
        cv.put(KEY_USER_EMAIL, email.toLowerCase().trim());
        cv.put(KEY_USER_PASSWORD_HASH, SecurityUtils.hashPassword(password));
        cv.put(KEY_USER_ROLE, "Staff");
        cv.put(KEY_USER_FACULTY, "Administration");
        cv.put(KEY_USER_CREATED_AT, System.currentTimeMillis());
        cv.put(KEY_USER_IS_ACTIVE, 1);
        cv.put(KEY_USER_MUST_CHANGE_PASSWORD, 1);
        cv.put(KEY_USER_POSITION, position != null ? position : "Staff Member");

        long staffId = db.insert(TABLE_USERS, null, cv);
        if (staffId == -1) return false;

        if (serviceId > 0) {
            assignStaffToService(staffId, serviceId);
        }
        return true;
    }

    public boolean updateStaffAccount(long staffId, String name, String email, String position, long serviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_USER_NAME, name);
        cv.put(KEY_USER_EMAIL, email.toLowerCase().trim());
        cv.put(KEY_USER_POSITION, position);

        int rows = db.update(TABLE_USERS, cv, KEY_ID + "=?", new String[]{String.valueOf(staffId)});
        if (rows > 0) {
            if (serviceId > 0) {
                assignStaffToService(staffId, serviceId);
            }
            return true;
        }
        return false;
    }

    public List<User> getAllAdminAccounts() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                "role LIKE '%Admin%' OR role='ADMIN'", null, null, null, KEY_USER_NAME + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(extractUserFromCursor(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public boolean createAdminAccount(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (userExists(email)) return false;

        ContentValues cv = new ContentValues();
        cv.put(KEY_USER_NAME, name);
        cv.put(KEY_USER_REG_NO, "");
        cv.put(KEY_USER_EMAIL, email.toLowerCase().trim());
        cv.put(KEY_USER_PASSWORD_HASH, SecurityUtils.hashPassword(password));
        cv.put(KEY_USER_ROLE, "ADMIN");
        cv.put(KEY_USER_FACULTY, "Administration");
        cv.put(KEY_USER_CREATED_AT, System.currentTimeMillis());
        cv.put(KEY_USER_IS_ACTIVE, 1);
        cv.put(KEY_USER_MUST_CHANGE_PASSWORD, 1);
        cv.put(KEY_USER_POSITION, "System Administrator");

        return db.insert(TABLE_USERS, null, cv) != -1;
    }

    public boolean toggleUserActiveStatus(long userId, boolean isActive) {
        // Safeguard: Prevent deactivating the last active admin
        User target = getUserById(userId);
        if (target != null && (target.role.equalsIgnoreCase("ADMIN") || target.role.equalsIgnoreCase("Administrator"))) {
            if (!isActive && getActiveAdminCount() <= 1) {
                Log.w("DatabaseHelper", "Cannot deactivate the last active administrator account.");
                return false;
            }
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_USER_IS_ACTIVE, isActive ? 1 : 0);
        return db.update(TABLE_USERS, cv, KEY_ID + "=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public int getActiveAdminCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE (role LIKE '%Admin%' OR role='ADMIN') AND is_active=1";
        return getCountByQuery(db, query);
    }

    public boolean resetUserPassword(long userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_USER_PASSWORD_HASH, SecurityUtils.hashPassword(newPassword));
        cv.put(KEY_USER_MUST_CHANGE_PASSWORD, 1);
        return db.update(TABLE_USERS, cv, KEY_ID + "=?", new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean updateUserPassword(long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (user == null) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_USER_PASSWORD_HASH, SecurityUtils.hashPassword(newPassword));
        cv.put(KEY_USER_MUST_CHANGE_PASSWORD, 0);
        return db.update(TABLE_USERS, cv, KEY_ID + "=?", new String[]{String.valueOf(userId)}) > 0;
    }

    // --- STAFF SERVICE ASSIGNMENTS ---

    public boolean assignStaffToService(long staffId, long serviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Remove existing assignments for this staff member to ensure 1 primary assignment or clear old ones
        db.delete(TABLE_STAFF_ASSIGNMENTS, KEY_ASSIGNMENT_STAFF_ID + "=?", new String[]{String.valueOf(staffId)});

        ContentValues cv = new ContentValues();
        cv.put(KEY_ASSIGNMENT_STAFF_ID, staffId);
        cv.put(KEY_ASSIGNMENT_SERVICE_ID, serviceId);
        cv.put(KEY_ASSIGNMENT_CREATED_AT, System.currentTimeMillis());
        return db.insert(TABLE_STAFF_ASSIGNMENTS, null, cv) != -1;
    }

    public List<ServiceOffice> getAssignedServicesForStaff(long staffId) {
        List<ServiceOffice> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s.* FROM " + TABLE_SERVICES + " s INNER JOIN "
                + TABLE_STAFF_ASSIGNMENTS + " a ON s." + KEY_ID + " = a." + KEY_ASSIGNMENT_SERVICE_ID
                + " WHERE a." + KEY_ASSIGNMENT_STAFF_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(staffId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ServiceOffice s = extractServiceFromCursor(cursor);
                s.setWaitingCount(getWaitingCountForService(s.getId()));
                list.add(s);
            }
            cursor.close();
        }
        return list;
    }

    public long getAssignedServiceIdForStaff(long staffId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + KEY_ASSIGNMENT_SERVICE_ID + " FROM " + TABLE_STAFF_ASSIGNMENTS + " WHERE " + KEY_ASSIGNMENT_STAFF_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(staffId)});
        long id = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                id = cursor.getLong(0);
            }
            cursor.close();
        }
        return id;
    }

    public boolean isStaffAssignedToService(long staffId, long serviceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_STAFF_ASSIGNMENTS + " WHERE "
                + KEY_ASSIGNMENT_STAFF_ID + "=? AND " + KEY_ASSIGNMENT_SERVICE_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(staffId), String.valueOf(serviceId)});
        boolean assigned = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                assigned = cursor.getInt(0) > 0;
            }
            cursor.close();
        }
        return assigned;
    }

    // --- SERVICE CRUD ---

    public List<ServiceOffice> getAllServices() {
        List<ServiceOffice> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SERVICES, null, null, null, null, null, KEY_SERVICE_NAME + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ServiceOffice s = extractServiceFromCursor(cursor);
                s.setWaitingCount(getWaitingCountForService(s.getId()));
                list.add(s);
            }
            cursor.close();
        }
        return list;
    }

    public ServiceOffice getServiceById(long serviceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SERVICES, null, KEY_ID + "=?", new String[]{String.valueOf(serviceId)}, null, null, null);
        ServiceOffice s = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                s = extractServiceFromCursor(cursor);
                s.setWaitingCount(getWaitingCountForService(s.getId()));
            }
            cursor.close();
        }
        return s;
    }

    public long addService(ServiceOffice s) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_SERVICE_NAME, s.getName());
        cv.put(KEY_SERVICE_LOCATION, s.getLocation());
        cv.put(KEY_SERVICE_STATUS, s.getStatus());
        cv.put(KEY_SERVICE_AVG_MINS, s.getAvgServiceTimeMins());
        cv.put(KEY_SERVICE_PHONE, s.getContactPhone());
        cv.put(KEY_SERVICE_EMAIL, s.getContactEmail());
        return db.insert(TABLE_SERVICES, null, cv);
    }

    public boolean updateService(ServiceOffice s) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_SERVICE_NAME, s.getName());
        cv.put(KEY_SERVICE_LOCATION, s.getLocation());
        cv.put(KEY_SERVICE_STATUS, s.getStatus());
        cv.put(KEY_SERVICE_AVG_MINS, s.getAvgServiceTimeMins());
        cv.put(KEY_SERVICE_PHONE, s.getContactPhone());
        cv.put(KEY_SERVICE_EMAIL, s.getContactEmail());
        return db.update(TABLE_SERVICES, cv, KEY_ID + "=?", new String[]{String.valueOf(s.getId())}) > 0;
    }

    public boolean updateServiceStatus(long serviceId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_SERVICE_STATUS, status);
        return db.update(TABLE_SERVICES, cv, KEY_ID + "=?", new String[]{String.valueOf(serviceId)}) > 0;
    }

    public boolean deleteService(long serviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Remove staff assignments for this service as well
        db.delete(TABLE_STAFF_ASSIGNMENTS, KEY_ASSIGNMENT_SERVICE_ID + "=?", new String[]{String.valueOf(serviceId)});
        return db.delete(TABLE_SERVICES, KEY_ID + "=?", new String[]{String.valueOf(serviceId)}) > 0;
    }

    private ServiceOffice extractServiceFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SERVICE_NAME));
        String loc = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SERVICE_LOCATION));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SERVICE_STATUS));
        int avgMins = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SERVICE_AVG_MINS));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SERVICE_PHONE));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SERVICE_EMAIL));
        return new ServiceOffice(id, name, loc, status, avgMins, phone, email);
    }

    // --- TICKET & QUEUE OPERATIONS ---

    public Ticket createTicket(long serviceId, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ServiceOffice office = getServiceById(serviceId);
        if (office == null) return null;

        String prefix = office.getName().replaceAll("[^A-Za-z]", "").toUpperCase();
        if (prefix.length() > 3) prefix = prefix.substring(0, 3);
        if (prefix.length() < 3) prefix = "CQ";

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TICKETS + " WHERE " + KEY_TICKET_SERVICE_ID + "=?", new String[]{String.valueOf(serviceId)});
        int seq = 1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                seq = cursor.getInt(0) + 1;
            }
            cursor.close();
        }

        String ticketNumber = String.format("%s-%03d", prefix, seq);
        int waitingAhead = getWaitingCountForService(serviceId);
        int position = waitingAhead + 1;
        long now = System.currentTimeMillis();

        ContentValues cv = new ContentValues();
        cv.put(KEY_TICKET_NUMBER, ticketNumber);
        cv.put(KEY_TICKET_SERVICE_ID, serviceId);
        cv.put(KEY_TICKET_USER_ID, userId);
        cv.put(KEY_TICKET_STATUS, TicketStatus.WAITING.name());
        cv.put(KEY_TICKET_POSITION, position);
        cv.put(KEY_TICKET_CREATED_AT, now);

        long id = db.insert(TABLE_TICKETS, null, cv);
        if (id == -1) return null;

        return getTicketById(id);
    }

    public Ticket getTicketById(long ticketId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TICKETS, null, KEY_ID + "=?", new String[]{String.valueOf(ticketId)}, null, null, null);
        Ticket ticket = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                ticket = extractTicketFromCursor(cursor);
            }
            cursor.close();
        }
        return ticket;
    }

    public Ticket getUserActiveTicket(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = KEY_TICKET_USER_ID + "=? AND " + KEY_TICKET_STATUS + " IN (?, ?, ?)";
        Cursor cursor = db.query(TABLE_TICKETS, null, query,
                new String[]{String.valueOf(userId), TicketStatus.WAITING.name(), TicketStatus.CALLED.name(), TicketStatus.IN_SERVICE.name()},
                null, null, KEY_ID + " DESC", "1");
        Ticket ticket = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                ticket = extractTicketFromCursor(cursor);
            }
            cursor.close();
        }
        return ticket;
    }

    public List<Ticket> getUserTicketHistory(long userId) {
        List<Ticket> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TICKETS, null, KEY_TICKET_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, KEY_ID + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(extractTicketFromCursor(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<Ticket> getWaitingTicketsForService(long serviceId) {
        List<Ticket> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = KEY_TICKET_SERVICE_ID + "=? AND " + KEY_TICKET_STATUS + " IN (?, ?, ?)";
        Cursor cursor = db.query(TABLE_TICKETS, null, query,
                new String[]{String.valueOf(serviceId), TicketStatus.WAITING.name(), TicketStatus.CALLED.name(), TicketStatus.IN_SERVICE.name()},
                null, null, KEY_ID + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(extractTicketFromCursor(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public int getWaitingCountForService(long serviceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_TICKETS + " WHERE " + KEY_TICKET_SERVICE_ID + "=? AND " + KEY_TICKET_STATUS + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(serviceId), TicketStatus.WAITING.name()});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public int getPositionInQueue(long serviceId, long ticketId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_TICKETS + " WHERE " + KEY_TICKET_SERVICE_ID + "=? AND " + KEY_TICKET_STATUS + "=? AND " + KEY_ID + " <= ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(serviceId), TicketStatus.WAITING.name(), String.valueOf(ticketId)});
        int pos = 1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                pos = cursor.getInt(0);
            }
            cursor.close();
        }
        return Math.max(1, pos);
    }

    public boolean updateTicketStatus(long ticketId, TicketStatus newStatus, String returnReason) {
        SQLiteDatabase db = this.getWritableDatabase();
        Ticket t = getTicketById(ticketId);
        if (t == null) return false;

        if (!t.getStatus().canTransitionTo(newStatus)) {
            Log.w("DatabaseHelper", "Invalid transition from " + t.getStatus() + " to " + newStatus);
            return false;
        }

        long now = System.currentTimeMillis();
        ContentValues cv = new ContentValues();
        cv.put(KEY_TICKET_STATUS, newStatus.name());

        if (newStatus == TicketStatus.CALLED) {
            cv.put(KEY_TICKET_CALLED_AT, now);
        } else if (newStatus == TicketStatus.IN_SERVICE) {
            cv.put(KEY_TICKET_SERVED_AT, now);
        } else if (newStatus == TicketStatus.COMPLETED) {
            cv.put(KEY_TICKET_COMPLETED_AT, now);
            long start = t.getServedAt() > 0 ? t.getServedAt() : (t.getCalledAt() > 0 ? t.getCalledAt() : now - 300000);
            int durationMins = Math.max(1, (int) ((now - start) / (1000 * 60)));
            recordCompletedService(db, ticketId, t.getServiceId(), durationMins, now);
        } else if (newStatus == TicketStatus.RETURN_REQUIRED) {
            cv.put(KEY_TICKET_RETURN_REASON, returnReason != null ? returnReason : "Documentation pending");
            cv.put(KEY_TICKET_COMPLETED_AT, now);
        }

        return db.update(TABLE_TICKETS, cv, KEY_ID + "=?", new String[]{String.valueOf(ticketId)}) > 0;
    }

    private void recordCompletedService(SQLiteDatabase db, long ticketId, long serviceId, int durationMins, long completedAt) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_RECORD_TICKET_ID, ticketId);
        cv.put(KEY_RECORD_SERVICE_ID, serviceId);
        cv.put(KEY_RECORD_DURATION, durationMins);
        cv.put(KEY_RECORD_COMPLETED_AT, completedAt);
        db.insert(TABLE_SERVICE_RECORDS, null, cv);
    }

    public List<Integer> getRecentServiceDurations(long serviceId, int limit) {
        List<Integer> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SERVICE_RECORDS, new String[]{KEY_RECORD_DURATION},
                KEY_RECORD_SERVICE_ID + "=?", new String[]{String.valueOf(serviceId)},
                null, null, KEY_RECORD_COMPLETED_AT + " DESC", String.valueOf(limit));
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursor.getInt(0));
            }
            cursor.close();
        }
        return list;
    }

    private Ticket extractTicketFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
        String number = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TICKET_NUMBER));
        long serviceId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TICKET_SERVICE_ID));
        long userId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TICKET_USER_ID));
        String statusStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TICKET_STATUS));
        TicketStatus status = TicketStatus.fromString(statusStr);
        int pos = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TICKET_POSITION));
        String returnReason = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TICKET_RETURN_REASON));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TICKET_CREATED_AT));
        long calledAt = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TICKET_CALLED_AT));
        long servedAt = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TICKET_SERVED_AT));
        long completedAt = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TICKET_COMPLETED_AT));

        ServiceOffice service = getServiceById(serviceId);
        String serviceName = service != null ? service.getName() : "Service #" + serviceId;

        String studentName = "Student #" + userId;
        Cursor uCursor = getReadableDatabase().query(TABLE_USERS, new String[]{KEY_USER_NAME}, KEY_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (uCursor != null) {
            if (uCursor.moveToFirst()) {
                studentName = uCursor.getString(0);
            }
            uCursor.close();
        }

        return new Ticket(id, number, serviceId, serviceName, userId, studentName, status, pos, returnReason, createdAt, calledAt, servedAt, completedAt);
    }
}
