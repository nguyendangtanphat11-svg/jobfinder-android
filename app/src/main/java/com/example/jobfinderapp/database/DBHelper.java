package com.example.jobfinderapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.jobfinderapp.models.Category;
import com.example.jobfinderapp.models.Company;
import com.example.jobfinderapp.models.Job;
import com.example.jobfinderapp.models.User;
import com.example.jobfinderapp.models.CV;
import com.example.jobfinderapp.models.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "jobfinder.db";
    private static final int DB_VERSION = 8; 

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, fullname TEXT NOT NULL, email TEXT UNIQUE, password TEXT, phone TEXT, avatar TEXT, role TEXT NOT NULL)");
        db.execSQL("CREATE TABLE companies (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, name TEXT NOT NULL, address TEXT, website TEXT, logo TEXT, description TEXT)");
        db.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL)");
        db.execSQL("CREATE TABLE jobs (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, company_id INTEGER, category_id INTEGER, salary TEXT, location TEXT, description TEXT, requirement TEXT, deadline TEXT, status TEXT DEFAULT 'Đang tuyển')");
        db.execSQL("CREATE TABLE cvs (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, cv_name TEXT, education TEXT, skills TEXT, experience TEXT, objective TEXT)");
        db.execSQL("CREATE TABLE applications (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, job_id INTEGER, apply_date TEXT, status TEXT)");
        db.execSQL("CREATE TABLE favorites (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, job_id INTEGER)");
        db.execSQL("CREATE TABLE notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, title TEXT, content TEXT, created_at TEXT, is_read INTEGER DEFAULT 0)");

        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notifications");
        db.execSQL("DROP TABLE IF EXISTS favorites");
        db.execSQL("DROP TABLE IF EXISTS applications");
        db.execSQL("DROP TABLE IF EXISTS cvs");
        db.execSQL("DROP TABLE IF EXISTS jobs");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS companies");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public void insertSampleData(SQLiteDatabase db) {
        String[] cats = {"CNTT", "AI", "Marketing", "Thiết kế", "Kế toán", "Kinh doanh"};
        for (String cat : cats) {
            ContentValues cv = new ContentValues();
            cv.put("name", cat);
            db.insert("categories", null, cv);
        }

        // Thêm 1 nhà tuyển dụng mẫu
        db.execSQL("INSERT INTO users (fullname, email, password, phone, role) VALUES ('FPT Software HR', 'employer@gmail.com', 'employer123', '0123456789', 'employer')");
        db.execSQL("INSERT INTO companies (user_id, name, address, website, logo, description) VALUES (1, 'FPT Software', 'Hà Nội', 'fpt.com', 'https://upload.wikimedia.org/wikipedia/commons/a/ad/FPT_Software_logo.png', 'Top IT Company')");
        
        // Thêm 1 ứng viên mẫu
        db.execSQL("INSERT INTO users (fullname, email, password, phone, role) VALUES ('Nguyễn Văn A', 'student@gmail.com', 'user123', '0987654321', 'candidate')");
        
        db.execSQL("INSERT INTO jobs (title, company_id, category_id, salary, location, description, requirement, deadline) VALUES ('Android Developer', 1, 1, '15-25 triệu', 'Hà Nội', 'Mô tả...', 'Yêu cầu...', '30/12/2024')");
    }

    // --- User Methods ---
    public boolean insertUser(String fullname, String email, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fullname", fullname);
        values.put("email", email);
        values.put("password", password);
        values.put("role", role);
        return db.insert("users", null, values) != -1;
    }

    public boolean insertUser(String fullname, String email, String password) {
        return insertUser(fullname, email, password, "candidate");
    }

    public User login(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", new String[]{email, password});
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = mapCursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ?", new String[]{email});
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = mapCursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ?", new String[]{email});
        int id = -1;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
        }
        return id;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = mapCursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    private User mapCursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        user.setFullname(cursor.getString(cursor.getColumnIndexOrThrow("fullname")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow("avatar")));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
        return user;
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fullname", user.getFullname());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("avatar", user.getAvatar());
        return db.update("users", values, "id = ?", new String[]{String.valueOf(user.getId())}) > 0;
    }

    // --- Employer & Company Methods ---
    public Company getCompanyByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM companies WHERE user_id = ?", new String[]{String.valueOf(userId)});
        Company company = null;
        if (cursor != null && cursor.moveToFirst()) {
            company = new Company();
            company.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            company.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            company.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
            company.setWebsite(cursor.getString(cursor.getColumnIndexOrThrow("website")));
            company.setLogo(cursor.getString(cursor.getColumnIndexOrThrow("logo")));
            company.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            cursor.close();
        } else if (cursor != null) {
            // Nếu chưa có công ty, tạo bản ghi trống cho Employer
            User user = getUserById(userId);
            if (user != null && "employer".equals(user.getRole())) {
                ContentValues values = new ContentValues();
                values.put("user_id", userId);
                values.put("name", "Tên công ty mới");
                long id = db.insert("companies", null, values);
                company = new Company((int) id, "Tên công ty mới", "", "", "", "");
            }
        }
        return company;
    }

    public boolean updateCompany(Company company) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", company.getName());
        values.put("address", company.getAddress());
        values.put("website", company.getWebsite());
        values.put("logo", company.getLogo());
        values.put("description", company.getDescription());
        return db.update("companies", values, "id = ?", new String[]{String.valueOf(company.getId())}) > 0;
    }

    public Map<String, Integer> getEmployerStats(int companyId) {
        Map<String, Integer> stats = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM jobs WHERE company_id = ?", new String[]{String.valueOf(companyId)});
        if (c1.moveToFirst()) stats.put("total_jobs", c1.getInt(0));
        c1.close();

        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM applications a JOIN jobs j ON a.job_id = j.id WHERE j.company_id = ?", new String[]{String.valueOf(companyId)});
        if (c2.moveToFirst()) stats.put("total_applicants", c2.getInt(0));
        c2.close();

        Cursor c3 = db.rawQuery("SELECT COUNT(*) FROM jobs WHERE company_id = ? AND status = 'Đang tuyển'", new String[]{String.valueOf(companyId)});
        if (c3.moveToFirst()) stats.put("active_jobs", c3.getInt(0));
        c3.close();

        return stats;
    }

    // --- Job Methods ---
    public List<Job> getJobsByCompanyId(int companyId) {
        List<Job> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM jobs WHERE company_id = ? ORDER BY id DESC", new String[]{String.valueOf(companyId)});
        if (cursor.moveToFirst()) {
            do {
                Job job = new Job();
                job.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                job.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                job.setSalary(cursor.getString(cursor.getColumnIndexOrThrow("salary")));
                job.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
                job.setDeadline(cursor.getString(cursor.getColumnIndexOrThrow("deadline")));
                job.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                list.add(job);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean addJob(Job job) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", job.getTitle());
        v.put("company_id", job.getCompanyId());
        v.put("category_id", job.getCategoryId());
        v.put("salary", job.getSalary());
        v.put("location", job.getLocation());
        v.put("description", job.getDescription());
        v.put("requirement", job.getRequirement());
        v.put("deadline", job.getDeadline());
        v.put("status", "Đang tuyển");
        return db.insert("jobs", null, v) != -1;
    }

    public boolean updateJob(Job job) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", job.getTitle());
        v.put("category_id", job.getCategoryId());
        v.put("salary", job.getSalary());
        v.put("location", job.getLocation());
        v.put("description", job.getDescription());
        v.put("requirement", job.getRequirement());
        v.put("deadline", job.getDeadline());
        v.put("status", job.getStatus());
        return db.update("jobs", v, "id = ?", new String[]{String.valueOf(job.getId())}) > 0;
    }

    public boolean deleteJob(int jobId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("jobs", "id = ?", new String[]{String.valueOf(jobId)}) > 0;
    }

    public List<Job> getAllJobs() {
        List<Job> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT j.*, c.name as company_name, c.logo as company_logo " +
                "FROM jobs j " +
                "JOIN companies c ON j.company_id = c.id " +
                "WHERE j.status = 'Đang tuyển' " +
                "ORDER BY j.id DESC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Job job = new Job();
                job.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                job.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                job.setSalary(cursor.getString(cursor.getColumnIndexOrThrow("salary")));
                job.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
                job.setDeadline(cursor.getString(cursor.getColumnIndexOrThrow("deadline")));
                list.add(job);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Map<String, Object>> getApplicantsByCompanyId(int companyId) {
        List<Map<String, Object>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT a.id, a.apply_date, a.status, u.fullname, u.email, j.title as job_title " +
                     "FROM applications a " +
                     "JOIN users u ON a.user_id = u.id " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "WHERE j.company_id = ? ORDER BY a.id DESC";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(companyId)});
        if (c.moveToFirst()) {
            do {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getInt(0));
                map.put("apply_date", c.getString(1));
                map.put("status", c.getString(2));
                map.put("fullname", c.getString(3));
                map.put("email", c.getString(4));
                map.put("job_title", c.getString(5));
                list.add(map);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean updateApplicationStatus(int appId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("status", status);
        return db.update("applications", v, "id = ?", new String[]{String.valueOf(appId)}) > 0;
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM categories", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Category cat = new Category();
                cat.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                cat.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                list.add(cat);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Job> searchJobs(String keyword) {
        List<Job> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT j.*, c.name as company_name, c.logo as company_logo " +
                "FROM jobs j " +
                "JOIN companies c ON j.company_id = c.id " +
                "WHERE (j.title LIKE ? OR c.name LIKE ? OR j.location LIKE ?) AND j.status = 'Đang tuyển' " +
                "ORDER BY j.id DESC";
        String wild = "%" + keyword + "%";
        Cursor cursor = db.rawQuery(query, new String[]{wild, wild, wild});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Job job = new Job();
                job.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                job.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                job.setSalary(cursor.getString(cursor.getColumnIndexOrThrow("salary")));
                job.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
                job.setDeadline(cursor.getString(cursor.getColumnIndexOrThrow("deadline")));
                list.add(job);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public Job getJobById(int jobId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT j.*, c.name as company_name, c.logo as company_logo " +
                "FROM jobs j " +
                "JOIN companies c ON j.company_id = c.id " +
                "WHERE j.id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(jobId)});
        Job job = null;
        if (cursor != null && cursor.moveToFirst()) {
            job = new Job();
            job.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            job.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            job.setCompanyId(cursor.getInt(cursor.getColumnIndexOrThrow("company_id")));
            job.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
            job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
            job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
            job.setSalary(cursor.getString(cursor.getColumnIndexOrThrow("salary")));
            job.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
            job.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            job.setRequirement(cursor.getString(cursor.getColumnIndexOrThrow("requirement")));
            job.setDeadline(cursor.getString(cursor.getColumnIndexOrThrow("deadline")));
            job.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
            cursor.close();
        }
        return job;
    }

    public boolean isFavorite(int userId, int jobId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM favorites WHERE user_id = ? AND job_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(jobId)});
        boolean favorite = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return favorite;
    }

    public boolean toggleFavorite(int userId, int jobId) {
        if (isFavorite(userId, jobId)) {
            return removeFavorite(userId, jobId);
        } else {
            return addToFavorite(userId, jobId);
        }
    }

    public boolean addToFavorite(int userId, int jobId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("job_id", jobId);
        return db.insert("favorites", null, values) != -1;
    }

    public boolean removeFavorite(int userId, int jobId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("favorites", "user_id = ? AND job_id = ?", new String[]{String.valueOf(userId), String.valueOf(jobId)}) > 0;
    }

    public List<Job> getFavoriteJobs(int userId) {
        List<Job> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT j.*, c.name as company_name, c.logo as company_logo " +
                "FROM favorites f " +
                "JOIN jobs j ON f.job_id = j.id " +
                "JOIN companies c ON j.company_id = c.id " +
                "WHERE f.user_id = ? " +
                "ORDER BY f.id DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Job job = new Job();
                job.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                job.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                job.setSalary(cursor.getString(cursor.getColumnIndexOrThrow("salary")));
                job.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
                job.setDeadline(cursor.getString(cursor.getColumnIndexOrThrow("deadline")));
                list.add(job);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Job> searchFavoriteJobs(int userId, String keyword) {
        List<Job> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT j.*, c.name as company_name, c.logo as company_logo " +
                "FROM favorites f " +
                "JOIN jobs j ON f.job_id = j.id " +
                "JOIN companies c ON j.company_id = c.id " +
                "WHERE f.user_id = ? AND (j.title LIKE ? OR c.name LIKE ?) " +
                "ORDER BY f.id DESC";
        String wild = "%" + keyword + "%";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), wild, wild});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Job job = new Job();
                job.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                job.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                job.setSalary(cursor.getString(cursor.getColumnIndexOrThrow("salary")));
                job.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
                job.setDeadline(cursor.getString(cursor.getColumnIndexOrThrow("deadline")));
                list.add(job);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public boolean isApplied(int userId, int jobId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM applications WHERE user_id = ? AND job_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(jobId)});
        boolean applied = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return applied;
    }

    public boolean applyJob(int userId, int jobId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("job_id", jobId);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        values.put("apply_date", sdf.format(new Date()));
        values.put("status", "Đang chờ");
        return db.insert("applications", null, values) != -1;
    }

    public List<Job> getApplicationHistory(int userId) {
        List<Job> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT j.*, c.name as company_name, c.logo as company_logo, a.apply_date, a.status as app_status " +
                "FROM applications a " +
                "JOIN jobs j ON a.job_id = j.id " +
                "JOIN companies c ON j.company_id = c.id " +
                "WHERE a.user_id = ? " +
                "ORDER BY a.id DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Job job = new Job();
                job.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                job.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                job.setSalary(cursor.getString(cursor.getColumnIndexOrThrow("salary")));
                job.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
                job.setApplyDate(cursor.getString(cursor.getColumnIndexOrThrow("apply_date")));
                job.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("app_status")));
                list.add(job);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // --- CV Methods ---
    public String getCVNameByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT cv_name FROM cvs WHERE user_id = ?", new String[]{String.valueOf(userId)});
        String cvName = "Chưa có CV";
        if (cursor != null && cursor.moveToFirst()) {
            cvName = cursor.getString(0);
            cursor.close();
        }
        return cvName;
    }

    public CV getCVByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM cvs WHERE user_id = ?", new String[]{String.valueOf(userId)});
        CV cv = null;
        if (cursor != null && cursor.moveToFirst()) {
            cv = new CV();
            cv.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            cv.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            cv.setCvName(cursor.getString(cursor.getColumnIndexOrThrow("cv_name")));
            cv.setEducation(cursor.getString(cursor.getColumnIndexOrThrow("education")));
            cv.setSkills(cursor.getString(cursor.getColumnIndexOrThrow("skills")));
            cv.setExperience(cursor.getString(cursor.getColumnIndexOrThrow("experience")));
            cv.setObjective(cursor.getString(cursor.getColumnIndexOrThrow("objective")));
            cursor.close();
        } else if (cursor != null) {
            // Nếu chưa có CV, tạo bản ghi trống
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("cv_name", "Chưa có tên");
            long id = db.insert("cvs", null, values);
            cv = new CV((int)id, userId, "Chưa có tên", "", "", "", "");
        }
        return cv;
    }

    public boolean updateCV(CV cv) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("cv_name", cv.getCvName());
        values.put("education", cv.getEducation());
        values.put("skills", cv.getSkills());
        values.put("experience", cv.getExperience());
        values.put("objective", cv.getObjective());
        return db.update("cvs", values, "id = ?", new String[]{String.valueOf(cv.getId())}) > 0;
    }

    // --- Notification Methods ---
    public List<Notification> getNotifications(int userId) {
        List<Notification> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM notifications WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Notification notif = new Notification();
                notif.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                notif.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                notif.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                notif.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
                notif.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                notif.setIsRead(cursor.getInt(cursor.getColumnIndexOrThrow("is_read")));
                list.add(notif);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public boolean addNotification(int userId, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title);
        values.put("content", content);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        values.put("created_at", sdf.format(new Date()));
        values.put("is_read", 0);
        return db.insert("notifications", null, values) != -1;
    }

    public boolean markAsRead(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_read", 1);
        return db.update("notifications", values, "id = ?", new String[]{String.valueOf(notificationId)}) > 0;
    }
}
