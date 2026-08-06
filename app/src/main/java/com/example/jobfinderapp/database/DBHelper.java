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
import com.example.jobfinderapp.models.CareerPreference;
import com.example.jobfinderapp.models.ProfileSettings;
import com.example.jobfinderapp.models.Education;
import com.example.jobfinderapp.models.Project;
import com.example.jobfinderapp.models.Certificate;
import com.example.jobfinderapp.models.Language;
import com.example.jobfinderapp.models.SocialLinks;

import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "jobfinder.db";
    private static final int DB_VERSION = 20;
    public static final String ROLE_CANDIDATE = "candidate";
    public static final String ROLE_EMPLOYER = "employer";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_BLOCKED = "blocked";
    public static final String ROLE_ADMIN = "admin";
    private static final String PASSWORD_HASH_PREFIX = "sha256$";
    private static DBHelper instance;

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        // Also seed newly introduced local accounts for databases created by older app versions.
        instance.seedSampleDataIfNeeded();
        return instance;
    }

    private DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, fullname TEXT, password TEXT, role TEXT, status TEXT NOT NULL DEFAULT 'active', phone TEXT, location TEXT, avatar TEXT, bio TEXT, education TEXT, date_of_birth TEXT, gender TEXT, address TEXT, professional_title TEXT, job_search_status TEXT DEFAULT 'actively_looking')");
        db.execSQL("CREATE TABLE companies (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER UNIQUE, name TEXT NOT NULL, address TEXT, website TEXT, logo TEXT, description TEXT, contact_email TEXT, contact_phone TEXT, industry TEXT, company_size TEXT, representative_name TEXT)");
        db.execSQL("CREATE TABLE categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL)");
        db.execSQL("CREATE TABLE jobs (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, company_id INTEGER, category_id INTEGER, salary TEXT, location TEXT, description TEXT, requirement TEXT, deadline TEXT, status TEXT DEFAULT 'Đang tuyển', experience TEXT, education TEXT, quantity INTEGER, age TEXT, job_type TEXT, gender TEXT)");
        db.execSQL("CREATE TABLE experiences (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, title TEXT, company TEXT, start_date TEXT, end_date TEXT)");
        db.execSQL("CREATE TABLE skills (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, skill_name TEXT)");
        db.execSQL("CREATE TABLE cvs (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, cv_name TEXT, education TEXT, skills TEXT, experience TEXT, objective TEXT)");
        db.execSQL("CREATE TABLE applications (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, job_id INTEGER, apply_date TEXT, status TEXT, fullname TEXT, phone TEXT, email TEXT, cv_path TEXT, message TEXT)");
        db.execSQL("CREATE TABLE favorites (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, job_id INTEGER)");
        db.execSQL("CREATE TABLE notifications (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, title TEXT, content TEXT, created_at TEXT, is_read INTEGER DEFAULT 0)");
        createProfileTables(db);

        seedSampleDataIfNeeded(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Migration 16 -> 17: preserve every existing user and mark it active.
        if (oldVersion < 17) {
            addUserColumnIfMissing(db, "status", "TEXT NOT NULL DEFAULT 'active'");
        }
        if (oldVersion < 18) {
            addUserColumnIfMissing(db, "location", "TEXT");
            addUserColumnIfMissing(db, "avatar", "TEXT");
            addUserColumnIfMissing(db, "bio", "TEXT");
            addUserColumnIfMissing(db, "education", "TEXT");
        }
        if (oldVersion < 19) {
            addCompanyColumnIfMissing(db, "contact_email", "TEXT"); addCompanyColumnIfMissing(db, "contact_phone", "TEXT");
            addCompanyColumnIfMissing(db, "industry", "TEXT"); addCompanyColumnIfMissing(db, "company_size", "TEXT"); addCompanyColumnIfMissing(db, "representative_name", "TEXT");
        }
        if (oldVersion < 20) {
            addUserColumnIfMissing(db, "date_of_birth", "TEXT");
            addUserColumnIfMissing(db, "gender", "TEXT");
            addUserColumnIfMissing(db, "address", "TEXT");
            addUserColumnIfMissing(db, "professional_title", "TEXT");
            addUserColumnIfMissing(db, "job_search_status", "TEXT DEFAULT 'actively_looking'");
            addColumnIfMissing(db, "skills", "level", "TEXT");
            addColumnIfMissing(db, "experiences", "location", "TEXT");
            addColumnIfMissing(db, "experiences", "description", "TEXT");
            addColumnIfMissing(db, "experiences", "achievements", "TEXT");
            addColumnIfMissing(db, "experiences", "is_current", "INTEGER DEFAULT 0");
            createProfileTables(db);
        }
    }

    private void addColumnIfMissing(SQLiteDatabase db, String table, String column, String definition) {
        try (Cursor c = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            while (c.moveToNext()) if (column.equalsIgnoreCase(c.getString(c.getColumnIndexOrThrow("name")))) return;
        }
        db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }

    private void createProfileTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS career_preferences (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER UNIQUE,desired_position TEXT,category_id INTEGER,career_level TEXT,job_type TEXT,expected_salary TEXT,desired_location TEXT,work_mode TEXT,years_experience TEXT,available_date TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS educations (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER,school_name TEXT,major TEXT,degree TEXT,start_date TEXT,end_date TEXT,gpa TEXT,description TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS projects (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER,project_name TEXT,role TEXT,description TEXT,technologies TEXT,start_date TEXT,end_date TEXT,github_url TEXT,demo_url TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS certificates (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER,certificate_name TEXT,organization TEXT,issue_date TEXT,expiry_date TEXT,credential_id TEXT,credential_url TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS languages (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER,language_name TEXT,proficiency TEXT,certificate TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS social_links (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER UNIQUE,github_url TEXT,linkedin_url TEXT,portfolio_url TEXT,website_url TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS profile_settings (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER UNIQUE,profile_visible INTEGER DEFAULT 1,show_phone INTEGER DEFAULT 1,show_email INTEGER DEFAULT 1,allow_job_invites INTEGER DEFAULT 1)");
    }

    private void addCompanyColumnIfMissing(SQLiteDatabase db, String column, String definition) {
        try (Cursor c = db.rawQuery("PRAGMA table_info(companies)", null)) { while (c.moveToNext()) if (column.equalsIgnoreCase(c.getString(c.getColumnIndexOrThrow("name")))) return; }
        db.execSQL("ALTER TABLE companies ADD COLUMN " + column + " " + definition);
    }

    private void addUserColumnIfMissing(SQLiteDatabase db, String column, String definition) {
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(users)", null)) {
            while (cursor.moveToNext()) {
                if (column.equalsIgnoreCase(cursor.getString(cursor.getColumnIndexOrThrow("name")))) return;
            }
        }
        db.execSQL("ALTER TABLE users ADD COLUMN " + column + " " + definition);
    }

    private void insertSampleData(SQLiteDatabase db) {
        String[] cats = {"CNTT", "AI", "Marketing", "Thiết kế", "Kế toán", "Bán hàng"};
        for (String cat : cats) {
            ContentValues cv = new ContentValues();
            cv.put("name", cat);
            db.insert("categories", null, cv);
        }

        ContentValues comp = new ContentValues();
        comp.put("user_id", 1);
        comp.put("name", "FPT Software");
        comp.put("address", "Quận 9, TP.HCM");
        comp.put("website", "https://fpt-software.com");
        comp.put("description", "Công ty công nghệ hàng đầu Việt Nam");
        db.insert("companies", null, comp);

        ContentValues user = new ContentValues();
        user.put("fullname", "Nguyễn Đặng Tấn Phát");
        user.put("email", "phat@gmail.com");
        user.put("password", hashPassword("123456"));
        user.put("role", ROLE_CANDIDATE);
        user.put("status", STATUS_ACTIVE);
        user.put("phone", "0912877344");
        user.put("education", "Đại học");
        db.insert("users", null, user);
    }

    public void insertSampleJobs() {
        // Kept for backward compatibility with callers. Job data must come from employers only.
        if (false && getAllJobs().isEmpty()) {
            Job j1 = new Job(0, "Lập trình viên Android (Java/Kotlin)", 1, 1, "15-25 triệu", "TP.HCM", "Phát triển ứng dụng di động trên nền tảng Android...", "Kinh nghiệm 1-2 năm, thành thạo Java...", "31/12/2024");
            j1.setJobType("Toàn thời gian");
            j1.setDeadline("31/12/2026");
            addJob(j1);

            Job j2 = new Job(0, "UI/UX Designer chuyên nghiệp", 1, 4, "12-20 triệu", "Đà Nẵng", "Thiết kế giao diện người dùng cho web và app...", "Sử dụng tốt Figma, Adobe XD...", "25/12/2024");
            j2.setJobType("Toàn thời gian");
            j2.setDeadline("25/12/2026");
            addJob(j2);

            Job j3 = new Job(0, "Chuyên viên Marketing Online", 1, 3, "10-18 triệu", "Hà Nội", "Quản lý các chiến dịch quảng cáo Facebook, Google...", "Có tư duy sáng tạo, khả năng viết bài tốt...", "30/12/2024");
            j3.setJobType("Bán thời gian");
            j3.setDeadline("30/12/2026");
            addJob(j3);

            Job j4 = new Job(0, "Kỹ sư AI & Machine Learning", 1, 2, "30-50 triệu", "TP.HCM", "Nghiên cứu và triển khai các mô hình AI...", "Thành thạo Python, có kiến thức sâu về Toán học...", "15/01/2025");
            j4.setJobType("Toàn thời gian");
            j4.setDeadline("15/01/2027");
            addJob(j4);

            Job j5 = new Job(0, "Kế toán tổng hợp", 1, 5, "8-12 triệu", "Hà Nội", "Quản lý sổ sách, chứng từ kế toán...", "Kinh nghiệm 1 năm ở vị trí tương đương...", "20/12/2024");
            j5.setJobType("Toàn thời gian");
            j5.setDeadline("20/12/2026");
            addJob(j5);
        }
    }

    // --- USER METHODS ---

    public long insertUser(User user) {
        if (user == null || !isAllowedRole(user.getRole())) {
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        String normalizedEmail = normalizeEmail(user.getEmail());
        if (normalizedEmail.isEmpty() || checkEmailExists(normalizedEmail)) {
            return -1;
        }

        String role = user.getRole();
        String status = ROLE_EMPLOYER.equals(role) ? STATUS_PENDING : STATUS_ACTIVE;
        String password = user.getPassword();

        v.put("email", normalizedEmail);
        v.put("fullname", user.getFullname());
        v.put("password", password == null ? null : hashPasswordIfNeeded(password));
        v.put("role", role);
        v.put("status", status);
        v.put("phone", user.getPhone());
        v.put("avatar", user.getAvatar());
        v.put("bio", user.getBio());
        v.put("education", user.getEducation());
        long userId = db.insert("users", null, v);
        
        if (userId != -1 && ROLE_EMPLOYER.equals(role)) {
            ContentValues cvComp = new ContentValues();
            cvComp.put("user_id", userId);
            cvComp.put("name", "Công ty của " + user.getFullname());
            db.insert("companies", null, cvComp);
        }
        
        return userId;
    }

    /** Seeds local presentation data atomically without touching user-owned data. */
    public void seedSampleDataIfNeeded() {
        seedSampleDataIfNeeded(getWritableDatabase());
    }

    private void seedSampleDataIfNeeded(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            ContentValues admin = new ContentValues();
            admin.put("email", "admin@jobfinder.com");
            admin.put("fullname", "Quản trị viên");
            admin.put("password", hashPassword("123456"));
            admin.put("role", ROLE_ADMIN);
            admin.put("status", STATUS_ACTIVE);
            try (Cursor existingAdmin = db.query("users", new String[]{"id"}, "email = ? AND role = ?", new String[]{"admin@jobfinder.com", ROLE_ADMIN}, null, null, null)) {
            if (!existingAdmin.moveToFirst()) {
                db.insert("users", null, admin);
            }
            }
            Map<String, Integer> categoryIds = new HashMap<>();
            removeSeedJobs(db);
            String[] categories = {"Công nghệ thông tin", "Trí tuệ nhân tạo", "Khoa học dữ liệu", "An toàn thông tin",
                    "Marketing", "Thiết kế", "Kế toán - Kiểm toán", "Tài chính - Ngân hàng", "Kinh doanh", "Bán lẻ - Dịch vụ",
                    "Nhân sự", "Chăm sóc khách hàng", "Hành chính - Văn phòng", "Pháp lý", "Giáo dục - Đào tạo",
                    "Y tế - Dược", "Kỹ thuật - Cơ khí", "Xây dựng - Kiến trúc", "Sản xuất - Vận hành", "Logistics - Xuất nhập khẩu",
                    "Du lịch - Nhà hàng - Khách sạn", "Truyền thông - Báo chí", "Bất động sản", "Nông nghiệp - Môi trường"};
            for (String category : categories) {
                categoryIds.put(category, ensureNamedRow(db, "categories", category, null));
            }

            Map<String, Integer> companyIds = new HashMap<>();
            String[][] companies = {
                    {"FPT Software", "Khu Công nghệ cao, TP.HCM", "https://fptsoftware.com", "https://upload.wikimedia.org/wikipedia/commons/7/77/FPT_logo_2010.svg", "Đơn vị phát triển phần mềm và chuyển đổi số."},
                    {"Viettel Digital", "Hà Nội", "https://viettel.com.vn", "https://upload.wikimedia.org/wikipedia/commons/6/68/Viettel_logo_2021.svg", "Dịch vụ số và giải pháp công nghệ."},
                    {"VNG Corporation", "Quận 7, TP.HCM", "https://vng.com.vn", "https://upload.wikimedia.org/wikipedia/commons/1/1a/VNG_Corporation_logo.svg", "Công ty công nghệ, nội dung số và nền tảng trực tuyến."},
                    {"Shopee Việt Nam", "Quận 1, TP.HCM", "https://shopee.vn", "https://upload.wikimedia.org/wikipedia/commons/f/fe/Shopee.svg", "Nền tảng thương mại điện tử tại Việt Nam."},
                    {"MoMo", "Quận 3, TP.HCM", "https://momo.vn", "https://upload.wikimedia.org/wikipedia/commons/1/1e/MoMo_Logo.png", "Ví điện tử và nền tảng dịch vụ tài chính số."}
            };
            for (String[] company : companies) {
                companyIds.put(company[0], ensureCompany(db, company));
            }

            if (false && isTableEmpty(db, "jobs")) {
                String[][] jobs = {
                        {"Android Developer Intern", "FPT Software", "Công nghệ thông tin", "5-8 triệu", "TP.HCM", "Phát triển ứng dụng Android cùng đội ngũ sản phẩm.", "Biết Java hoặc Kotlin, ham học hỏi.", "31/12/2026", "Không yêu cầu", "Sinh viên", "3", "20-24", "Thực tập", "Không yêu cầu"},
                        {"Java Backend Developer", "Viettel Digital", "Công nghệ thông tin", "15-25 triệu", "Hà Nội", "Xây dựng dịch vụ backend hiệu năng cao.", "Java Spring Boot, SQL, REST API.", "15/01/2027", "1-2 năm", "Đại học", "2", "22-32", "Toàn thời gian", "Không yêu cầu"},
                        {"ReactJS Frontend Developer", "VNG Corporation", "Công nghệ thông tin", "15-25 triệu", "TP.HCM", "Phát triển giao diện web cho sản phẩm số.", "ReactJS, TypeScript và CSS hiện đại.", "20/12/2026", "1-2 năm", "Đại học", "2", "22-30", "Toàn thời gian", "Không yêu cầu"},
                        {"Node.js Developer", "Shopee Việt Nam", "Công nghệ thông tin", "25-40 triệu", "Làm việc từ xa", "Xây dựng API cho hệ thống thương mại điện tử.", "Node.js, database và microservices.", "10/02/2027", "3 năm", "Đại học", "2", "24-35", "Làm việc từ xa", "Không yêu cầu"},
                        {"Software Tester Intern", "MoMo", "Công nghệ thông tin", "5-8 triệu", "TP.HCM", "Kiểm thử chức năng cho ứng dụng thanh toán.", "Cẩn thận, hiểu cơ bản kiểm thử phần mềm.", "30/11/2026", "Không yêu cầu", "Sinh viên", "4", "20-24", "Thực tập", "Không yêu cầu"},
                        {"UI/UX Designer", "VNG Corporation", "Thiết kế", "15-25 triệu", "TP.HCM", "Thiết kế trải nghiệm cho nền tảng số.", "Figma, portfolio thiết kế sản phẩm.", "25/01/2027", "1-2 năm", "Đại học", "1", "22-30", "Toàn thời gian", "Không yêu cầu"},
                        {"Product Designer", "Shopee Việt Nam", "Thiết kế", "25-40 triệu", "Hà Nội", "Thiết kế sản phẩm thương mại điện tử.", "Nắm vững nghiên cứu người dùng và Figma.", "05/02/2027", "3 năm", "Đại học", "1", "24-35", "Toàn thời gian", "Không yêu cầu"},
                        {"AI Engineer", "FPT Software", "Trí tuệ nhân tạo", "25-40 triệu", "Đà Nẵng", "Phát triển mô hình AI cho khách hàng doanh nghiệp.", "Python, machine learning và xử lý dữ liệu.", "28/02/2027", "1-2 năm", "Đại học", "2", "22-35", "Toàn thời gian", "Không yêu cầu"},
                        {"Data Analyst", "MoMo", "Trí tuệ nhân tạo", "15-25 triệu", "TP.HCM", "Phân tích dữ liệu hành vi người dùng.", "SQL, Excel và trực quan hóa dữ liệu.", "15/12/2026", "1-2 năm", "Đại học", "2", "22-32", "Toàn thời gian", "Không yêu cầu"},
                        {"Digital Marketing Intern", "Shopee Việt Nam", "Marketing", "5-8 triệu", "Hà Nội", "Hỗ trợ chiến dịch marketing số.", "Yêu thích marketing, biết dùng mạng xã hội.", "20/12/2026", "Không yêu cầu", "Sinh viên", "3", "20-24", "Thực tập", "Không yêu cầu"},
                        {"Content Marketing", "Viettel Digital", "Marketing", "10-15 triệu", "Hà Nội", "Sáng tạo nội dung cho sản phẩm công nghệ.", "Kỹ năng viết tốt và tư duy sáng tạo.", "10/01/2027", "Dưới 1 năm", "Cao đẳng", "2", "21-28", "Toàn thời gian", "Không yêu cầu"},
                        {"Nhân viên kinh doanh", "FPT Software", "Kinh doanh", "10-15 triệu", "Cần Thơ", "Tư vấn giải pháp công nghệ cho khách hàng.", "Giao tiếp tốt, yêu thích kinh doanh.", "31/01/2027", "Dưới 1 năm", "Cao đẳng", "3", "21-30", "Toàn thời gian", "Không yêu cầu"},
                        {"Business Analyst", "MoMo", "Kinh doanh", "15-25 triệu", "TP.HCM", "Phân tích yêu cầu và phối hợp phát triển sản phẩm.", "Tư duy phân tích, viết tài liệu tốt.", "18/02/2027", "1-2 năm", "Đại học", "2", "22-32", "Toàn thời gian", "Không yêu cầu"},
                        {"Chuyên viên tuyển dụng", "Viettel Digital", "Nhân sự", "10-15 triệu", "Hà Nội", "Tìm kiếm và đồng hành cùng nhân sự công nghệ.", "Có kỹ năng phỏng vấn và giao tiếp.", "22/12/2026", "1-2 năm", "Đại học", "2", "22-30", "Toàn thời gian", "Nữ"},
                        {"HR Intern", "VNG Corporation", "Nhân sự", "5-8 triệu", "TP.HCM", "Hỗ trợ vận hành hoạt động tuyển dụng.", "Nhanh nhẹn, sử dụng tốt văn phòng.", "05/01/2027", "Không yêu cầu", "Sinh viên", "2", "20-24", "Bán thời gian", "Không yêu cầu"},
                        {"Kế toán tổng hợp", "FPT Software", "Kế toán", "10-15 triệu", "Đà Nẵng", "Theo dõi chứng từ và lập báo cáo kế toán.", "Nắm nguyên lý kế toán, sử dụng Excel.", "28/12/2026", "1-2 năm", "Đại học", "1", "22-32", "Toàn thời gian", "Không yêu cầu"},
                        {"Kế toán thanh toán", "Shopee Việt Nam", "Kế toán", "15-25 triệu", "TP.HCM", "Xử lý thanh toán và đối soát giao dịch.", "Kế toán, cẩn thận và chịu trách nhiệm.", "12/01/2027", "1-2 năm", "Đại học", "2", "22-32", "Toàn thời gian", "Nữ"},
                        {"Customer Support", "MoMo", "Chăm sóc khách hàng", "10-15 triệu", "Cần Thơ", "Hỗ trợ khách hàng qua điện thoại và chat.", "Giọng nói rõ ràng, kỹ năng xử lý tình huống.", "14/02/2027", "Dưới 1 năm", "Cao đẳng", "4", "20-30", "Bán thời gian", "Không yêu cầu"},
                        {"Mobile Developer", "VNG Corporation", "Công nghệ thông tin", "25-40 triệu", "Làm việc từ xa", "Phát triển tính năng mobile cho người dùng.", "Kotlin hoặc Flutter, hiểu REST API.", "25/02/2027", "3 năm", "Đại học", "2", "24-35", "Làm việc từ xa", "Không yêu cầu"},
                        {"DevOps Engineer", "Viettel Digital", "Công nghệ thông tin", "25-40 triệu", "Hà Nội", "Vận hành hạ tầng cloud và quy trình CI/CD.", "Docker, Kubernetes, Linux và CI/CD.", "20/01/2027", "3 năm", "Đại học", "1", "24-35", "Toàn thời gian", "Nam"},
                        {"PHP Developer", "FPT Software", "Công nghệ thông tin", "10-15 triệu", "Đà Nẵng", "Phát triển ứng dụng web cho khách hàng.", "PHP Laravel, MySQL và Git.", "08/02/2027", "1-2 năm", "Cao đẳng", "2", "22-30", "Toàn thời gian", "Không yêu cầu"},
                        {"Database Administrator", "MoMo", "Công nghệ thông tin", "25-40 triệu", "TP.HCM", "Quản trị và tối ưu hệ thống cơ sở dữ liệu.", "MySQL hoặc PostgreSQL, sao lưu dữ liệu.", "27/02/2027", "3 năm", "Đại học", "1", "24-35", "Toàn thời gian", "Không yêu cầu"}
                };
                for (String[] job : jobs) insertSeedJob(db, job, companyIds, categoryIds);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Removes only jobs created by the built-in demo companies (they have no owner user_id). */
    private void removeSeedJobs(SQLiteDatabase db) {
        String where = "job_id IN (SELECT j.id FROM jobs j JOIN companies c ON c.id=j.company_id WHERE c.user_id IS NULL)";
        db.delete("favorites", where, null);
        db.delete("applications", where, null);
        db.delete("jobs", "company_id IN (SELECT id FROM companies WHERE user_id IS NULL)", null);
    }

    private int ensureNamedRow(SQLiteDatabase db, String table, String name, ContentValues values) {
        try (Cursor cursor = db.rawQuery("SELECT id FROM " + table + " WHERE name = ?", new String[]{name})) {
            if (cursor.moveToFirst()) return cursor.getInt(0);
        }
        ContentValues insertValues = values == null ? new ContentValues() : values;
        insertValues.put("name", name);
        return (int) db.insertOrThrow(table, null, insertValues);
    }

    private int ensureCompany(SQLiteDatabase db, String[] company) {
        ContentValues values = new ContentValues();
        values.putNull("user_id"); values.put("address", company[1]); values.put("website", company[2]);
        values.put("logo", company[3]); values.put("description", company[4]);
        return ensureNamedRow(db, "companies", company[0], values);
    }

    private boolean isTableEmpty(SQLiteDatabase db, String table) {
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + table, null)) {
            return cursor.moveToFirst() && cursor.getInt(0) == 0;
        }
    }

    private void insertSeedJob(SQLiteDatabase db, String[] job, Map<String, Integer> companyIds, Map<String, Integer> categoryIds) {
        ContentValues values = new ContentValues();
        values.put("title", job[0]); values.put("company_id", companyIds.get(job[1])); values.put("category_id", categoryIds.get(job[2]));
        values.put("salary", job[3]); values.put("location", job[4]); values.put("description", job[5]); values.put("requirement", job[6]);
        values.put("deadline", job[7]); values.put("status", "Đang tuyển"); values.put("experience", job[8]); values.put("education", job[9]);
        values.put("quantity", Integer.parseInt(job[10])); values.put("age", job[11]); values.put("job_type", job[12]); values.put("gender", job[13]);
        db.insertOrThrow("jobs", null, values);
    }

    public boolean insertUser(String fullName, String email, String password, String role) {
        User user = new User();
        user.setFullname(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return insertUser(user) != -1;
    }

    public boolean checkEmailExists(String email) {
        String normalizedEmail = normalizeEmail(email);
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT id FROM users WHERE LOWER(TRIM(email)) = ?", new String[]{normalizedEmail})) {
            return cursor.moveToFirst();
        }
    }

    public User login(String email, String password) {
        User user = getUserByEmail(email);
        if (user == null || !matchesPassword(password, user.getPassword())) {
            return null;
        }

        // Upgrade a legacy plaintext password after a successful login.
        if (!isHashedPassword(user.getPassword())) {
            ContentValues values = new ContentValues();
            values.put("password", hashPassword(password));
            getWritableDatabase().update("users", values, "id = ?", new String[]{String.valueOf(user.getId())});
            user.setPassword(values.getAsString("password"));
        }
        return user;
    }

    public User getUserByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM users WHERE LOWER(TRIM(email)) = ?", new String[]{normalizedEmail})) {
            if (cursor.moveToFirst()) {
                return fillUserFromCursor(cursor);
            }
        }
        return null;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM users WHERE id = ?", new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                return fillUserFromCursor(cursor);
            }
        }
        return null;
    }

    private User fillUserFromCursor(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        user.setFullname(cursor.getString(cursor.getColumnIndexOrThrow("fullname")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
        user.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
        int locationIndex = cursor.getColumnIndex("location");
        user.setLocation(locationIndex >= 0 ? cursor.getString(locationIndex) : null);
        user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow("avatar")));
        user.setBio(cursor.getString(cursor.getColumnIndexOrThrow("bio")));
        user.setEducation(cursor.getString(cursor.getColumnIndexOrThrow("education")));
        int i=cursor.getColumnIndex("date_of_birth"); user.setDateOfBirth(i>=0?cursor.getString(i):null);
        i=cursor.getColumnIndex("gender"); user.setGender(i>=0?cursor.getString(i):null);
        i=cursor.getColumnIndex("address"); user.setAddress(i>=0?cursor.getString(i):null);
        i=cursor.getColumnIndex("professional_title"); user.setProfessionalTitle(i>=0?cursor.getString(i):null);
        i=cursor.getColumnIndex("job_search_status"); user.setJobSearchStatus(i>=0?cursor.getString(i):"actively_looking");
        return user;
    }

    public boolean updateUserInfo(int userId, String name, String phone, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("fullname", name);
        v.put("phone", phone);
        v.put("email", normalizeEmail(email));
        return db.update("users", v, "id = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean updateUserBio(int userId, String bio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("bio", bio);
        return db.update("users", v, "id = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean updateUserEducation(int userId, String education) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("education", education);
        return db.update("users", v, "id = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("fullname", user.getFullname());
        v.put("email", normalizeEmail(user.getEmail()));
        v.put("phone", user.getPhone());
        v.put("avatar", user.getAvatar());
        v.put("bio", user.getBio());
        v.put("education", user.getEducation());
        return db.update("users", v, "id = ?", new String[]{String.valueOf(user.getId())}) > 0;
    }

    /** Updates candidate-editable fields only; authentication and authorization columns are untouched. */
    public boolean updateUserProfile(int userId, String name, String phone, String location,
                                     String bio, String education, String avatar) {
        ContentValues values = new ContentValues();
        values.put("fullname", name == null ? "" : name.trim());
        values.put("phone", phone == null ? "" : phone.trim());
        values.put("location", location == null ? "" : location.trim());
        values.put("bio", bio == null ? "" : bio.trim());
        values.put("education", education == null ? "" : education.trim());
        values.put("avatar", avatar == null ? "" : avatar.trim());
        return getWritableDatabase().update("users", values, "id = ?", new String[]{String.valueOf(userId)}) > 0;
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isAllowedRole(String role) {
        return ROLE_CANDIDATE.equals(role) || ROLE_EMPLOYER.equals(role);
    }

    public List<Map<String, Object>> getPendingEmployers() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT u.id user_id,u.fullname,u.email,u.phone,u.status,c.name company_name,c.address,c.website,c.description,c.logo "
                + "FROM users u LEFT JOIN companies c ON c.user_id=u.id WHERE u.role=? AND u.status=? ORDER BY u.id DESC";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{ROLE_EMPLOYER, STATUS_PENDING})) {
            while (c.moveToNext()) {
                Map<String,Object> row = new HashMap<>();
                String[] names = {"user_id","fullname","email","phone","status","company_name","address","website","description","logo"};
                for (int i=0;i<names.length;i++) row.put(names[i], c.getString(i));
                result.add(row);
            }
        }
        return result;
    }

    public boolean updateEmployerStatus(int userId, String status) {
        if (!STATUS_ACTIVE.equals(status) && !STATUS_REJECTED.equals(status) && !STATUS_BLOCKED.equals(status)) return false;
        ContentValues values = new ContentValues(); values.put("status", status);
        return getWritableDatabase().update("users", values, "id=? AND role=? AND status=?",
                new String[]{String.valueOf(userId), ROLE_EMPLOYER, STATUS_PENDING}) > 0;
    }

    public Map<String, Integer> getAdminStats() {
        Map<String,Integer> stats = new HashMap<>();
        stats.put("users", getCount("users", null, null));
        stats.put("candidates", getCount("users", "role=?", new String[]{ROLE_CANDIDATE}));
        stats.put("employers", getCount("users", "role=?", new String[]{ROLE_EMPLOYER}));
        stats.put("pending", getCount("users", "role=? AND status=?", new String[]{ROLE_EMPLOYER,STATUS_PENDING}));
        stats.put("active_employers", getCount("users", "role=? AND status=?", new String[]{ROLE_EMPLOYER,STATUS_ACTIVE}));
        stats.put("blocked", getCount("users", "status=?", new String[]{STATUS_BLOCKED}));
        stats.put("jobs", getCount("jobs", null, null)); stats.put("applications", getCount("applications", null, null));
        return stats;
    }
    public int getJobCountByStatus(String status) { return getCount("jobs", "status=?", new String[]{status}); }
    public int getApplicationCount() { return getCount("applications", null, null); }
    public int getPendingEmployerCount() { return getCount("users", "role=? AND status=?", new String[]{ROLE_EMPLOYER, STATUS_PENDING}); }
    public int getBlockedUserCount() { return getCount("users", "status=?", new String[]{STATUS_BLOCKED}); }
    public List<Map<String, Object>> getRecentApplicationsForAdmin(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT a.fullname,a.apply_date,j.title job_title FROM applications a LEFT JOIN jobs j ON j.id=a.job_id ORDER BY a.id DESC LIMIT " + Math.max(1, Math.min(limit, 10));
        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) { while (c.moveToNext()) { Map<String,Object> row = new HashMap<>(); row.put("fullname", c.getString(0)); row.put("apply_date", c.getString(1)); row.put("job_title", c.getString(2)); result.add(row); } }
        return result;
    }
    public List<Map<String,Object>> getAllUsersForAdmin(String keyword,String role,String status){List<Map<String,Object>> out=new ArrayList<>();String sql="SELECT u.id,u.fullname,u.email,u.phone,u.role,u.status,u.avatar,c.name company_name FROM users u LEFT JOIN companies c ON c.user_id=u.id WHERE 1=1";List<String>a=new ArrayList<>();if(keyword!=null&&!keyword.trim().isEmpty()){sql+=" AND (LOWER(u.fullname) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(u.phone) LIKE ? OR LOWER(COALESCE(c.name,'')) LIKE ?)";String p="%"+keyword.trim().toLowerCase(Locale.ROOT)+"%";for(int i=0;i<4;i++)a.add(p);}if(role!=null&&!role.isEmpty()){sql+=" AND u.role=?";a.add(role);}if(status!=null&&!status.isEmpty()){sql+=" AND u.status=?";a.add(status);}sql+=" ORDER BY u.id DESC";try(Cursor c=getReadableDatabase().rawQuery(sql,a.toArray(new String[0]))){while(c.moveToNext()){Map<String,Object>m=new HashMap<>();for(int i=0;i<c.getColumnCount();i++)m.put(c.getColumnName(i),c.getString(i));out.add(m);}}return out;}
    public int getActiveAdminCount(){return getCount("users","role=? AND status=?",new String[]{ROLE_ADMIN,STATUS_ACTIVE});}
    public int getCandidateApplicationCount(int id){return getCount("applications","user_id=?",new String[]{String.valueOf(id)});}
    public int getCandidateApplicationCountByStatus(int id, String status){return getCount("applications","user_id=? AND status=?",new String[]{String.valueOf(id),status});}
    public int getEmployerJobCount(int id){String sql="SELECT COUNT(*) FROM jobs j JOIN companies c ON j.company_id=c.id WHERE c.user_id=?";try(Cursor c=getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(id)})){return c.moveToFirst()?c.getInt(0):0;}}
    public boolean updateUserStatusByAdmin(int id,String status,int actor){if(!STATUS_ACTIVE.equals(status)&&!STATUS_BLOCKED.equals(status)&&!STATUS_REJECTED.equals(status))return false;if(id==actor)return false;User target=getUserById(id);if(target==null||ROLE_ADMIN.equals(target.getRole()))return false;if(STATUS_BLOCKED.equals(status)&&getActiveAdminCount()<=1)return false;ContentValues values=new ContentValues();values.put("status",status);return getWritableDatabase().update("users",values,"id=? AND role IN (?,?)",new String[]{String.valueOf(id),ROLE_CANDIDATE,ROLE_EMPLOYER})>0;}

    private int getCount(String table, String where, String[] args) {
        String sql = "SELECT COUNT(*) FROM " + table + (where == null ? "" : " WHERE " + where);
        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) { return c.moveToFirst() ? c.getInt(0) : 0; }
    }

    private static String hashPasswordIfNeeded(String password) {
        return isHashedPassword(password) || "GOOGLE_AUTH_NO_PASS".equals(password)
                ? password : hashPassword(password);
    }

    private static boolean matchesPassword(String input, String storedPassword) {
        if (input == null || storedPassword == null || "GOOGLE_AUTH_NO_PASS".equals(storedPassword)) {
            return false;
        }
        return isHashedPassword(storedPassword)
                ? storedPassword.equals(hashPassword(input))
                : storedPassword.equals(input);
    }

    private static boolean isHashedPassword(String password) {
        return password != null && password.startsWith(PASSWORD_HASH_PREFIX);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(PASSWORD_HASH_PREFIX);
            for (byte item : hash) {
                result.append(String.format(Locale.ROOT, "%02x", item));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    // --- JOB METHODS ---

    public List<Job> getAllJobs() {
        List<Job> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT j.*, c.name as company_name, c.logo as company_logo, c.address as company_address FROM jobs j JOIN companies c ON j.company_id = c.id ORDER BY j.id DESC";
        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Job job = fillJobFromCursor(cursor);
                    job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                    job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                    job.setCompanyAddress(cursor.getString(cursor.getColumnIndexOrThrow("company_address")));
                    list.add(job);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public List<Job> searchJobs(String keyword) {
        return getJobsForHome(keyword, 0);
    }
    public List<Map<String,Object>> getAllJobsForAdmin(String keyword,String status,int categoryId){List<Map<String,Object>>o=new ArrayList<>();String q="SELECT j.id,j.title,j.location,j.salary,j.job_type,j.deadline,j.status,c.name company_name,c.logo company_logo,cat.name category_name,u.email employer_email FROM jobs j LEFT JOIN companies c ON c.id=j.company_id LEFT JOIN categories cat ON cat.id=j.category_id LEFT JOIN users u ON u.id=c.user_id WHERE 1=1";List<String>a=new ArrayList<>();if(keyword!=null&&!keyword.trim().isEmpty()){q+=" AND (LOWER(j.title) LIKE ? OR LOWER(COALESCE(c.name,'')) LIKE ? OR LOWER(COALESCE(j.location,'')) LIKE ? OR LOWER(COALESCE(u.email,'')) LIKE ?)";String p="%"+keyword.trim().toLowerCase(Locale.ROOT)+"%";for(int i=0;i<4;i++)a.add(p);}if(status!=null&&!status.isEmpty()){q+=" AND j.status=?";a.add(status);}if(categoryId>0){q+=" AND j.category_id=?";a.add(String.valueOf(categoryId));}q+=" ORDER BY j.id DESC";try(Cursor c=getReadableDatabase().rawQuery(q,a.toArray(new String[0]))){while(c.moveToNext()){Map<String,Object>m=new HashMap<>();for(int i=0;i<c.getColumnCount();i++)m.put(c.getColumnName(i),c.getString(i));o.add(m);}}return o;}
    public boolean updateJobStatusByAdmin(int jobId,String status){if(!"Đang tuyển".equals(status)&&!"Đã đóng".equals(status)&&!"Bị ẩn".equals(status))return false;ContentValues v=new ContentValues();v.put("status",status);return getWritableDatabase().update("jobs",v,"id=?",new String[]{String.valueOf(jobId)})>0;}

    public List<Job> getJobsByCategoryId(int categoryId) {
        return getJobsForHome("", categoryId);
    }

    /** Candidate-home query: case-insensitive title, company and location search with optional category. */
    public List<Job> getJobsForHome(String keyword, int categoryId) {
        List<Job> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        StringBuilder query = new StringBuilder(
                "SELECT j.*, c.name AS company_name, c.logo AS company_logo, c.address AS company_address " +
                        "FROM jobs j JOIN companies c ON j.company_id = c.id WHERE 1 = 1");
        List<String> arguments = new ArrayList<>();
        if (!normalizedKeyword.isEmpty()) {
            query.append(" AND (LOWER(j.title) LIKE ? OR LOWER(c.name) LIKE ? OR LOWER(j.location) LIKE ?)");
            String wildcard = "%" + normalizedKeyword + "%";
            arguments.add(wildcard);
            arguments.add(wildcard);
            arguments.add(wildcard);
        }
        if (categoryId > 0) {
            query.append(" AND j.category_id = ?");
            arguments.add(String.valueOf(categoryId));
        }
        query.append(" ORDER BY j.id DESC");
        try (Cursor cursor = db.rawQuery(query.toString(), arguments.toArray(new String[0]))) {
            while (cursor.moveToNext()) {
                Job job = fillJobFromCursor(cursor);
                job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                job.setCompanyAddress(cursor.getString(cursor.getColumnIndexOrThrow("company_address")));
                list.add(job);
            }
        }
        return list;
    }

    public List<Job> getFilteredJobs(String keyword, String salary, String jobType) {
        List<Job> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT j.*, c.name as company_name, c.logo as company_logo, c.address as company_address FROM jobs j JOIN companies c ON j.company_id = c.id WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            query.append(" AND (j.title LIKE ? OR j.location LIKE ? OR c.name LIKE ?)");
            String wild = "%" + keyword + "%";
            args.add(wild);
            args.add(wild);
            args.add(wild);
        }

        if (salary != null && !salary.equals("Tất cả")) {
            // Simplified salary filter: matches jobs containing the salary string
            query.append(" AND j.salary LIKE ?");
            args.add("%" + salary + "%");
        }

        if (jobType != null && !jobType.equals("Tất cả")) {
            query.append(" AND j.job_type = ?");
            args.add(jobType);
        }

        query.append(" ORDER BY j.id DESC");

        try (Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]))) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Job job = fillJobFromCursor(cursor);
                    job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                    job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                    job.setCompanyAddress(cursor.getString(cursor.getColumnIndexOrThrow("company_address")));
                    list.add(job);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public List<Job> getJobsByCompanyId(int companyId) {
        List<Job> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT j.*, c.name as company_name, c.logo as company_logo, c.address as company_address " +
                "FROM jobs j " +
                "JOIN companies c ON j.company_id = c.id " +
                "WHERE j.company_id = ? " +
                "ORDER BY j.id DESC";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(companyId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Job job = fillJobFromCursor(cursor);
                    job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                    job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                    job.setCompanyAddress(cursor.getString(cursor.getColumnIndexOrThrow("company_address")));
                    list.add(job);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public Job getJobById(int jobId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT j.*, c.name as company_name, c.logo as company_logo, c.address as company_address FROM jobs j JOIN companies c ON j.company_id = c.id WHERE j.id = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(jobId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                Job job = fillJobFromCursor(cursor);
                job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                job.setCompanyAddress(cursor.getString(cursor.getColumnIndexOrThrow("company_address")));
                return job;
            }
        }
        return null;
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
        v.put("experience", job.getExperience());
        v.put("education", job.getEducation());
        v.put("quantity", job.getQuantity());
        v.put("age", job.getAge());
        v.put("job_type", job.getJobType());
        v.put("gender", job.getGender());
        return db.insert("jobs", null, v) != -1;
    }

    public boolean updateJob(Job job) {
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
        v.put("status", job.getStatus());
        v.put("experience", job.getExperience());
        v.put("education", job.getEducation());
        v.put("quantity", job.getQuantity());
        v.put("age", job.getAge());
        v.put("job_type", job.getJobType());
        v.put("gender", job.getGender());
        return db.update("jobs", v, "id = ?", new String[]{String.valueOf(job.getId())}) > 0;
    }

    public boolean deleteJob(int jobId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("jobs", "id = ?", new String[]{String.valueOf(jobId)}) > 0;
    }

    private Job fillJobFromCursor(Cursor cursor) {
        Job job = new Job();
        job.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        job.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        job.setCompanyId(cursor.getInt(cursor.getColumnIndexOrThrow("company_id")));
        job.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
        job.setSalary(cursor.getString(cursor.getColumnIndexOrThrow("salary")));
        job.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
        job.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        job.setRequirement(cursor.getString(cursor.getColumnIndexOrThrow("requirement")));
        job.setDeadline(cursor.getString(cursor.getColumnIndexOrThrow("deadline")));
        job.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        
        job.setExperience(cursor.getString(cursor.getColumnIndexOrThrow("experience")));
        job.setEducation(cursor.getString(cursor.getColumnIndexOrThrow("education")));
        job.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
        job.setAge(cursor.getString(cursor.getColumnIndexOrThrow("age")));
        job.setJobType(cursor.getString(cursor.getColumnIndexOrThrow("job_type")));
        job.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
        
        return job;
    }

    // --- EXPERIENCES & SKILLS ---

    public boolean addExperience(int userId, String title, String company, String start, String end) {
        if (!isValidExperience(userId, title, company, start, end)) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("title", title.trim());
        v.put("company", company.trim());
        v.put("start_date", start.trim());
        if (isBlank(end)) v.putNull("end_date"); else v.put("end_date", end.trim());
        return db.insert("experiences", null, v) != -1;
    }

    public boolean updateExperience(int userId, int experienceId, String title, String company, String start, String end) {
        if (experienceId <= 0 || !isValidExperience(userId, title, company, start, end)) return false;
        ContentValues values = new ContentValues();
        values.put("title", title.trim()); values.put("company", company.trim()); values.put("start_date", start.trim());
        if (isBlank(end)) values.putNull("end_date"); else values.put("end_date", end.trim());
        return getWritableDatabase().update("experiences", values, "id = ? AND user_id = ?",
                new String[]{String.valueOf(experienceId), String.valueOf(userId)}) > 0;
    }

    public boolean updateExperience(int userId, int experienceId, String title, String company, String location, String start, String end, String description, String achievements) {
        if (!updateExperience(userId, experienceId, title, company, start, end)) return false;
        ContentValues values = new ContentValues(); values.put("location", location); values.put("description", description); values.put("achievements", achievements);
        return getWritableDatabase().update("experiences", values, "id = ? AND user_id = ?", new String[]{String.valueOf(experienceId),String.valueOf(userId)}) > 0;
    }

    private boolean isValidExperience(int userId, String title, String company, String start, String end) {
        if (userId <= 0 || isBlank(title) || isBlank(company) || isBlank(start) || !isValidExperienceDate(start)) return false;
        return isBlank(end) || (isValidExperienceDate(end) && compareExperienceDates(end, start) >= 0);
    }

    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }

    private boolean isValidExperienceDate(String value) {
        String[] parts = value.trim().split("/");
        if (parts.length != 2) return false;
        try { int month = Integer.parseInt(parts[0]); int year = Integer.parseInt(parts[1]); return month >= 1 && month <= 12 && year >= 1900 && year <= 9999; }
        catch (NumberFormatException ignored) { return false; }
    }

    private int compareExperienceDates(String left, String right) {
        String[] leftParts = left.trim().split("/"); String[] rightParts = right.trim().split("/");
        return Integer.compare(Integer.parseInt(leftParts[1]) * 12 + Integer.parseInt(leftParts[0]), Integer.parseInt(rightParts[1]) * 12 + Integer.parseInt(rightParts[0]));
    }

    public List<Map<String, String>> getExperiences(int userId) {
        List<Map<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM experiences WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Map<String, String> exp = new HashMap<>();
                    exp.put("id", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("id"))));
                    exp.put("title", cursor.getString(cursor.getColumnIndexOrThrow("title")));
                    exp.put("company", cursor.getString(cursor.getColumnIndexOrThrow("company")));
                    exp.put("start_date", cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
                    exp.put("end_date", cursor.getString(cursor.getColumnIndexOrThrow("end_date")));
                    int locationIndex = cursor.getColumnIndex("location"); if (locationIndex >= 0) exp.put("location", cursor.getString(locationIndex));
                    int descriptionIndex = cursor.getColumnIndex("description"); if (descriptionIndex >= 0) exp.put("description", cursor.getString(descriptionIndex));
                    int achievementsIndex = cursor.getColumnIndex("achievements"); if (achievementsIndex >= 0) exp.put("achievements", cursor.getString(achievementsIndex));
                    String endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                    exp.put("period", cursor.getString(cursor.getColumnIndexOrThrow("start_date")) + " - " + (isBlank(endDate) ? "Hiện tại" : endDate));
                    list.add(exp);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public boolean addSkill(int userId, String skill) {
        if (skill == null || skill.trim().isEmpty() || skillExists(userId, skill)) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("skill_name", skill.trim());
        return db.insert("skills", null, v) != -1;
    }

    public List<String> getSkills(int userId) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT skill_name FROM skills WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public Company getCompanyByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM companies WHERE user_id = ?", new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                Company company = new Company();
                company.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                company.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                company.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                company.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                company.setWebsite(cursor.getString(cursor.getColumnIndexOrThrow("website")));
                company.setLogo(cursor.getString(cursor.getColumnIndexOrThrow("logo")));
                company.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                int i=cursor.getColumnIndex("contact_email"); if(i>=0) company.setContactEmail(cursor.getString(i));
                i=cursor.getColumnIndex("contact_phone"); if(i>=0) company.setContactPhone(cursor.getString(i));
                i=cursor.getColumnIndex("industry"); if(i>=0) company.setIndustry(cursor.getString(i));
                i=cursor.getColumnIndex("company_size"); if(i>=0) company.setCompanySize(cursor.getString(i));
                i=cursor.getColumnIndex("representative_name"); if(i>=0) company.setRepresentativeName(cursor.getString(i));
                return company;
            }
        }
        return null;
    }

    public boolean updateCompany(Company company) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", company.getName());
        v.put("address", company.getAddress());
        v.put("website", company.getWebsite());
        v.put("logo", company.getLogo());
        v.put("description", company.getDescription()); v.put("contact_email", company.getContactEmail()); v.put("contact_phone", company.getContactPhone());
        v.put("industry", company.getIndustry()); v.put("company_size", company.getCompanySize()); v.put("representative_name", company.getRepresentativeName());
        return db.update("companies", v, "id = ? AND user_id = ?", new String[]{String.valueOf(company.getId()), String.valueOf(company.getUserId())}) > 0;
    }

    public int getApplicationCountByJobId(int jobId) {
        try (Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM applications WHERE job_id=?",new String[]{String.valueOf(jobId)})){return c.moveToFirst()?c.getInt(0):0;}
    }
    public boolean updateJobStatus(int jobId,int companyId,String status){if(!"Đang tuyển".equals(status)&&!"Đã đóng".equals(status))return false;ContentValues v=new ContentValues();v.put("status",status);return getWritableDatabase().update("jobs",v,"id=? AND company_id=?",new String[]{String.valueOf(jobId),String.valueOf(companyId)})>0;}
    public boolean deleteJobSafely(int jobId,int companyId){if(getApplicationCountByJobId(jobId)>0)return false;return getWritableDatabase().delete("jobs","id=? AND company_id=?",new String[]{String.valueOf(jobId),String.valueOf(companyId)})>0;}
    public String getCategoryNameById(int id){try(Cursor c=getReadableDatabase().rawQuery("SELECT name FROM categories WHERE id=?",new String[]{String.valueOf(id)})){return c.moveToFirst()?c.getString(0):"Đang cập nhật";}}

    public boolean companyExistsForUser(int userId) { return getCompanyByUserId(userId) != null; }
    public boolean upsertCompany(Company company) {
        if (company == null || company.getUserId() <= 0 || company.getName() == null || company.getName().trim().isEmpty()) return false;
        Company existing = getCompanyByUserId(company.getUserId());
        if (existing != null) { company.setId(existing.getId()); return updateCompany(company); }
        ContentValues v=new ContentValues(); v.put("user_id",company.getUserId()); v.put("name",company.getName().trim()); v.put("address",company.getAddress()); v.put("website",company.getWebsite()); v.put("logo",company.getLogo()); v.put("description",company.getDescription()); v.put("contact_email",company.getContactEmail()); v.put("contact_phone",company.getContactPhone()); v.put("industry",company.getIndustry()); v.put("company_size",company.getCompanySize()); v.put("representative_name",company.getRepresentativeName());
        long id=getWritableDatabase().insert("companies",null,v); if(id>0){company.setId((int)id);return true;} return false;
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM categories", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Category cat = new Category();
                    cat.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    cat.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                    list.add(cat);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    // --- CV METHODS ---

    public CV getCVByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM cvs WHERE user_id = ?", new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                return fillCVFromCursor(cursor);
            }
        }
        return null;
    }

    private CV fillCVFromCursor(Cursor cursor) {
        CV cv = new CV();
        cv.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        cv.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        cv.setCvName(cursor.getString(cursor.getColumnIndexOrThrow("cv_name")));
        cv.setEducation(cursor.getString(cursor.getColumnIndexOrThrow("education")));
        cv.setSkills(cursor.getString(cursor.getColumnIndexOrThrow("skills")));
        cv.setExperience(cursor.getString(cursor.getColumnIndexOrThrow("experience")));
        cv.setObjective(cursor.getString(cursor.getColumnIndexOrThrow("objective")));
        return cv;
    }

    public boolean updateCV(CV cv) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("user_id", cv.getUserId());
        v.put("cv_name", cv.getCvName());
        v.put("education", cv.getEducation());
        v.put("skills", cv.getSkills());
        v.put("experience", cv.getExperience());
        v.put("objective", cv.getObjective());

        if (cv.getId() <= 0) {
            CV existing = getCVByUserId(cv.getUserId());
            if (existing != null) {
                return db.update("cvs", v, "user_id = ?", new String[]{String.valueOf(cv.getUserId())}) > 0;
            }
            return db.insert("cvs", null, v) != -1;
        } else {
            return db.update("cvs", v, "id = ?", new String[]{String.valueOf(cv.getId())}) > 0;
        }
    }

    // --- APPLICATION METHODS ---

    public List<Job> getApplicationHistory(int userId) {
        return searchApplications(userId, "", null);
    }

    /** Preserves old application records even when their job or company has been removed. */
    public List<Job> searchApplications(int userId, String keyword, String status) {
        List<Job> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String normalizedStatus = "CASE a.status "
                + "WHEN 'Đã gửi' THEN 'Đang chờ' "
                + "WHEN 'Đang xét duyệt' THEN 'Đang xem xét' "
                + "WHEN 'Đã nhận' THEN 'Đã chấp nhận' "
                + "WHEN 'Đã từ chối' THEN 'Đã từ chối' "
                + "ELSE COALESCE(a.status, 'Đang chờ') END";
        StringBuilder query = new StringBuilder("SELECT "
                + "a.id AS application_id, a.job_id AS applied_job_id, a.apply_date, " + normalizedStatus + " AS app_status, "
                + "COALESCE(j.id, 0) AS id, COALESCE(j.title, 'Công việc không còn khả dụng') AS title, "
                + "COALESCE(j.company_id, 0) AS company_id, COALESCE(j.category_id, 0) AS category_id, "
                + "COALESCE(j.salary, 'Đang cập nhật') AS salary, COALESCE(j.location, 'Đang cập nhật') AS location, "
                + "COALESCE(j.description, '') AS description, COALESCE(j.requirement, '') AS requirement, "
                + "COALESCE(j.deadline, '') AS deadline, COALESCE(j.status, '') AS status, "
                + "COALESCE(j.experience, '') AS experience, COALESCE(j.education, '') AS education, "
                + "COALESCE(j.quantity, 0) AS quantity, COALESCE(j.age, '') AS age, "
                + "COALESCE(j.job_type, '') AS job_type, COALESCE(j.gender, '') AS gender, "
                + "COALESCE(c.name, 'Công ty không còn khả dụng') AS company_name, "
                + "c.logo AS company_logo, COALESCE(c.address, 'Đang cập nhật') AS company_address "
                + "FROM applications a LEFT JOIN jobs j ON a.job_id = j.id "
                + "LEFT JOIN companies c ON j.company_id = c.id WHERE a.user_id = ?");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));
        String safeKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (!safeKeyword.isEmpty()) {
            query.append(" AND (LOWER(COALESCE(j.title, '')) LIKE ? OR LOWER(COALESCE(c.name, '')) LIKE ?)");
            String pattern = "%" + safeKeyword + "%";
            args.add(pattern);
            args.add(pattern);
        }
        if (status != null && !status.trim().isEmpty()) {
            query.append(" AND ").append(normalizedStatus).append(" = ?");
            args.add(status.trim());
        }
        query.append(" ORDER BY a.id DESC");
        try (Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]))) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Job job = fillJobFromCursor(cursor);
                    job.setId(cursor.getInt(cursor.getColumnIndexOrThrow("applied_job_id")));
                    job.setApplyDate(cursor.getString(cursor.getColumnIndexOrThrow("apply_date")));
                    job.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("app_status")));
                    job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                    job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                    job.setCompanyAddress(cursor.getString(cursor.getColumnIndexOrThrow("company_address")));
                    list.add(job);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public boolean applyJob(int userId, int jobId, String name, String phone, String email, String cvPath, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            try (Cursor cursor = db.rawQuery("SELECT id FROM applications WHERE user_id = ? AND job_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(jobId)})) {
                if (cursor.moveToFirst()) return false;
            }
            ContentValues v = new ContentValues();
            v.put("user_id", userId); v.put("job_id", jobId); v.put("fullname", name); v.put("phone", phone);
            v.put("email", normalizeEmail(email)); v.put("cv_path", cvPath); v.put("message", message);
            v.put("apply_date", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));
            v.put("status", "Đang chờ");
            boolean inserted = db.insert("applications", null, v) != -1;
            if (inserted) db.setTransactionSuccessful();
            return inserted;
        } finally {
            db.endTransaction();
        }
    }

    public boolean hasAlreadyApplied(int userId, int jobId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT id FROM applications WHERE user_id = ? AND job_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(jobId)})) {
            boolean applied = cursor != null && cursor.getCount() > 0;
            if (cursor != null) cursor.close();
            return applied;
        }
    }

    public boolean addNotification(int userId, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("title", title);
        v.put("content", content);
        v.put("created_at", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()));
        v.put("is_read", 0);
        return db.insert("notifications", null, v) != -1;
    }

    public List<Notification> getNotifications(int userId) {
        List<Notification> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM notifications WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Notification notification = new Notification();
                    notification.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    notification.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                    notification.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                    notification.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
                    notification.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                    notification.setIsRead(cursor.getInt(cursor.getColumnIndexOrThrow("is_read")));
                    list.add(notification);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public boolean markAsRead(int notificationId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("is_read", 1);
        return db.update("notifications", v, "id = ? AND user_id = ? AND is_read = 0",
                new String[]{String.valueOf(notificationId), String.valueOf(userId)}) > 0;
    }

    public int markAllNotificationsAsRead(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("is_read", 1);
        return db.update("notifications", v, "user_id = ? AND is_read = 0",
                new String[]{String.valueOf(userId)});
    }

    public boolean deleteNotification(int notificationId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("notifications", "id = ? AND user_id = ?",
                new String[]{String.valueOf(notificationId), String.valueOf(userId)}) > 0;
    }

    public int deleteAllNotifications(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("notifications", "user_id = ?", new String[]{String.valueOf(userId)});
    }

    public int getUnreadNotificationCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0",
                new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public List<Map<String, Object>> getApplicantsByCompanyId(int companyId) {
        List<Map<String, Object>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT a.id, a.fullname, a.email, a.phone, a.cv_path, a.message, j.title as job_title, a.apply_date, a.status " +
                "FROM applications a " +
                "JOIN jobs j ON a.job_id = j.id " +
                "WHERE j.company_id = ? " +
                "ORDER BY a.id DESC";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(companyId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    map.put("fullname", cursor.getString(cursor.getColumnIndexOrThrow("fullname")));
                    map.put("email", cursor.getString(cursor.getColumnIndexOrThrow("email")));
                    map.put("phone", cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                    map.put("cv_path", cursor.getString(cursor.getColumnIndexOrThrow("cv_path")));
                    map.put("message", cursor.getString(cursor.getColumnIndexOrThrow("message")));
                    map.put("job_title", cursor.getString(cursor.getColumnIndexOrThrow("job_title")));
                    map.put("apply_date", cursor.getString(cursor.getColumnIndexOrThrow("apply_date")));
                    map.put("status", cursor.getString(cursor.getColumnIndexOrThrow("status")));
                    list.add(map);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public boolean updateApplicationStatus(int appId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("status", status);
        boolean updated = db.update("applications", v, "id = ?", new String[]{String.valueOf(appId)}) > 0;
        
        if (updated) {
            String query = "SELECT a.user_id, j.title FROM applications a JOIN jobs j ON a.job_id = j.id WHERE a.id = ?";
            try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(appId)})) {
                if (cursor != null && cursor.moveToFirst()) {
                    int userId = cursor.getInt(0);
                    String jobTitle = cursor.getString(1);
                    String notiTitle = "Cập nhật đơn ứng tuyển";
                    String notiContent = "Đơn ứng tuyển cho vị trí '" + jobTitle + "' đã được cập nhật trạng thái: " + status;
                    addNotification(userId, notiTitle, notiContent);
                }
            }
        }
        return updated;
    }

    // --- FAVORITE METHODS ---

    public boolean isFavorite(int userId, int jobId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM favorites WHERE user_id = ? AND job_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(jobId)})) {
            boolean fav = cursor != null && cursor.getCount() > 0;
            if (cursor != null) cursor.close();
            return fav;
        }
    }

    public boolean addToFavorite(int userId, int jobId) {
        if (isFavorite(userId, jobId)) {
            return true;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("job_id", jobId);
        return db.insert("favorites", null, v) != -1;
    }

    public boolean removeFavorite(int userId, int jobId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("favorites", "user_id = ? AND job_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(jobId)}) > 0;
    }

    public boolean toggleFavorite(int userId, int jobId) {
        if (isFavorite(userId, jobId)) {
            return removeFavorite(userId, jobId);
        } else {
            return addToFavorite(userId, jobId);
        }
    }

    public List<Job> getFavoriteJobs(int userId) {
        return getFavoriteJobsForCandidate(userId, "", null);
    }

    public List<Map<String,Object>> getEmployerApplicants(int companyId,String keyword,String status){List<Map<String,Object>> all=getApplicantsByCompanyId(companyId);String q=keyword==null?"":keyword.trim().toLowerCase(Locale.ROOT);List<Map<String,Object>> out=new ArrayList<>();for(Map<String,Object> r:all){String s=String.valueOf(r.get("status"));String text=(String.valueOf(r.get("fullname"))+" "+String.valueOf(r.get("email"))+" "+String.valueOf(r.get("job_title"))).toLowerCase(Locale.ROOT);if((q.isEmpty()||text.contains(q))&&(status==null||status.isEmpty()||status.equals(s)))out.add(r);}return out;}
    public Map<String,Object> getApplicationDetail(int appId,int companyId){String sql="SELECT a.*,j.title job_title,u.avatar,u.email user_email,c.cv_name,c.education cv_education,c.skills cv_skills,c.experience cv_experience,c.objective cv_objective FROM applications a JOIN jobs j ON a.job_id=j.id LEFT JOIN users u ON a.user_id=u.id LEFT JOIN cvs c ON c.user_id=a.user_id WHERE a.id=? AND j.company_id=?";try(Cursor x=getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(appId),String.valueOf(companyId)})){if(x.moveToFirst()){Map<String,Object> m=new HashMap<>();for(int i=0;i<x.getColumnCount();i++)m.put(x.getColumnName(i),x.getString(i));return m;}}return null;}
    public boolean updateApplicationStatusForCompany(int appId,int companyId,String status){if(!"Đang chờ".equals(status)&&!"Đang xem xét".equals(status)&&!"Đã chấp nhận".equals(status)&&!"Đã từ chối".equals(status))return false;String sql="SELECT a.user_id,j.title,a.status FROM applications a JOIN jobs j ON a.job_id=j.id WHERE a.id=? AND j.company_id=?";try(Cursor c=getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(appId),String.valueOf(companyId)})){if(!c.moveToFirst())return false;int uid=c.getInt(0);String title=c.getString(1),old=c.getString(2);if(status.equals(old))return true;ContentValues v=new ContentValues();v.put("status",status);boolean ok=getWritableDatabase().update("applications",v,"id=?",new String[]{String.valueOf(appId)})>0;if(ok)addNotification(uid,"Cập nhật đơn ứng tuyển","Đơn cho vị trí '"+title+"' đã chuyển sang trạng thái: "+status);return ok;}}

    /**
     * Retrieves a candidate's saved jobs. Orphan rows are removed only for the
     * supplied user, while a missing company is displayed with safe fallbacks.
     */
    public List<Job> getFavoriteJobsForCandidate(int userId, String keyword, String filter) {
        List<Job> list = new ArrayList<>();
        cleanupOrphanFavorites(userId);
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder query = new StringBuilder("SELECT j.*, COALESCE(c.name, 'Công ty đang cập nhật') AS company_name, "
                + "c.logo AS company_logo, COALESCE(c.address, 'Đang cập nhật') AS company_address "
                + "FROM favorites f JOIN jobs j ON f.job_id = j.id "
                + "LEFT JOIN companies c ON j.company_id = c.id WHERE f.user_id = ?");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));
        String safeKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (!safeKeyword.isEmpty()) {
            query.append(" AND (LOWER(COALESCE(j.title, '')) LIKE ? OR LOWER(COALESCE(c.name, '')) LIKE ? "
                    + "OR LOWER(COALESCE(j.location, '')) LIKE ?)");
            String pattern = "%" + safeKeyword + "%";
            args.add(pattern);
            args.add(pattern);
            args.add(pattern);
        }
        query.append(" ORDER BY f.id DESC");
        try (Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]))) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Job job = fillJobFromCursor(cursor);
                    job.setCompanyName(cursor.getString(cursor.getColumnIndexOrThrow("company_name")));
                    job.setCompanyLogo(cursor.getString(cursor.getColumnIndexOrThrow("company_logo")));
                    job.setCompanyAddress(cursor.getString(cursor.getColumnIndexOrThrow("company_address")));
                    boolean recruiting = isRecruitingAndNotExpired(job);
                    if ("recruiting".equals(filter) && !recruiting) continue;
                    if ("closed".equals(filter) && recruiting) continue;
                    job.setStatus(recruiting ? "Đang tuyển" : "Hết hạn hoặc đã đóng");
                    list.add(job);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public boolean skillExists(int userId, String skill) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT id FROM skills WHERE user_id = ? AND LOWER(skill_name) = ?",
                new String[]{String.valueOf(userId), skill == null ? "" : skill.trim().toLowerCase(Locale.ROOT)})) {
            return cursor.moveToFirst();
        }
    }

    public boolean deleteSkill(int userId, String skill) {
        return getWritableDatabase().delete("skills", "user_id = ? AND skill_name = ?",
                new String[]{String.valueOf(userId), skill}) > 0;
    }

    public boolean deleteExperience(int userId, int experienceId) {
        return getWritableDatabase().delete("experiences", "id = ? AND user_id = ?",
                new String[]{String.valueOf(experienceId), String.valueOf(userId)}) > 0;
    }

    public List<Job> searchFavoriteJobs(int userId, String keyword) {
        return getFavoriteJobsForCandidate(userId, keyword, null);
    }

    public int getFavoriteCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM favorites WHERE user_id = ?",
                new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    /** Deletes only the current user's saved rows whose jobs no longer exist. */
    public int cleanupOrphanFavorites(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("favorites", "user_id = ? AND NOT EXISTS (SELECT 1 FROM jobs WHERE jobs.id = favorites.job_id)",
                new String[]{String.valueOf(userId)});
    }

    private boolean isRecruitingAndNotExpired(Job job) {
        return "Đang tuyển".equals(job.getStatus()) && !isDeadlineExpired(job.getDeadline());
    }

    private boolean isDeadlineExpired(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) return false;
        String[] formats = {"dd/MM/yyyy", "yyyy-MM-dd"};
        for (String format : formats) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ROOT);
                dateFormat.setLenient(false);
                Date parsed = dateFormat.parse(deadline.trim());
                if (parsed != null) {
                    java.util.Calendar today = java.util.Calendar.getInstance();
                    today.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    today.set(java.util.Calendar.MINUTE, 0);
                    today.set(java.util.Calendar.SECOND, 0);
                    today.set(java.util.Calendar.MILLISECOND, 0);
                    return parsed.before(today.getTime());
                }
            } catch (Exception ignored) {
                // Unknown legacy date formats are treated as not expired.
            }
        }
        return false;
    }

    public Map<String, Integer> getEmployerStats(int companyId) {
        Map<String, Integer> stats = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM jobs WHERE company_id = ?", new String[]{String.valueOf(companyId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                stats.put("total_jobs", cursor.getInt(0));
            }
        }

        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM applications a JOIN jobs j ON a.job_id = j.id WHERE j.company_id = ?", new String[]{String.valueOf(companyId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                stats.put("total_applicants", cursor.getInt(0));
            }
        }

        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM jobs WHERE company_id = ? AND status = 'Đang tuyển'", new String[]{String.valueOf(companyId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                stats.put("active_jobs", cursor.getInt(0));
            }
        }

        return stats;
    }

    public int getPendingApplicantCount(int companyId) {
        return getApplicationCountForCompany(companyId, "Đang chờ");
    }

    public int getAcceptedApplicantCount(int companyId) {
        return getApplicationCountForCompany(companyId, "Chấp nhận");
    }

    public int getEmployerJobCountByStatus(int companyId, String status) {
        String sql = "SELECT COUNT(*) FROM jobs WHERE company_id=? AND status=?";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(companyId), status})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    public Map<String, Integer> getEmployerDashboardStats(int employerUserId) {
        Map<String, Integer> stats = new HashMap<>();
        Company company = getCompanyByUserId(employerUserId);
        if (company == null) { stats.put("total_jobs", 0); stats.put("active_jobs", 0); stats.put("total_applicants", 0); stats.put("pending_applicants", 0); return stats; }
        int companyId = company.getId();
        stats.put("total_jobs", getEmployerJobCount(employerUserId));
        stats.put("active_jobs", getEmployerJobCountByStatus(companyId, "Đang tuyển"));
        stats.put("total_applicants", getApplicationCountForCompany(companyId, null));
        stats.put("pending_applicants", getApplicationCountForCompany(companyId, "Đang chờ"));
        stats.put("review_applicants", getApplicationCountForCompany(companyId, "Đang xem xét"));
        stats.put("accepted_applicants", getApplicationCountForCompany(companyId, "Đã chấp nhận"));
        stats.put("rejected_applicants", getApplicationCountForCompany(companyId, "Đã từ chối"));
        return stats;
    }

    public int getEmployerApplicationCountByStatus(int companyId, String status) {
        return getApplicationCountForCompany(companyId, status);
    }

    private int getApplicationCountForCompany(int companyId, String status) {
        String sql = "SELECT COUNT(*) FROM applications a JOIN jobs j ON a.job_id=j.id WHERE j.company_id=?" + (status == null ? "" : " AND a.status=?");
        String[] args = status == null ? new String[]{String.valueOf(companyId)} : new String[]{String.valueOf(companyId), status};
        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    public List<Job> getRecentJobsByCompanyId(int companyId, int limit) {
        List<Job> jobs = new ArrayList<>();
        String safeLimit = String.valueOf(Math.max(1, Math.min(limit, 50)));
        String sql = "SELECT j.*, c.name company_name, c.logo company_logo, c.address company_address "
                + "FROM jobs j LEFT JOIN companies c ON j.company_id=c.id WHERE j.company_id=? "
                + "ORDER BY j.id DESC LIMIT " + safeLimit;
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(companyId)})) {
            while (c.moveToNext()) jobs.add(fillJobFromCursor(c));
        }
        return jobs;
    }

    public List<Map<String, Object>> getRecentApplicantsByCompanyId(int companyId, int limit) {
        List<Map<String, Object>> all = getApplicantsByCompanyId(companyId);
        if (all.size() > limit) return new ArrayList<>(all.subList(0, Math.max(0, limit)));
        return all;
    }
    public CareerPreference getCareerPreference(int userId) {
        try (Cursor c=getReadableDatabase().rawQuery("SELECT * FROM career_preferences WHERE user_id=?",new String[]{String.valueOf(userId)})) {
            if(!c.moveToFirst()) return null; CareerPreference p=new CareerPreference(); p.setId(c.getInt(c.getColumnIndexOrThrow("id"))); p.setUserId(userId);
            p.setDesiredPosition(c.getString(c.getColumnIndexOrThrow("desired_position"))); p.setCareerLevel(c.getString(c.getColumnIndexOrThrow("career_level"))); p.setJobType(c.getString(c.getColumnIndexOrThrow("job_type"))); p.setExpectedSalary(c.getString(c.getColumnIndexOrThrow("expected_salary"))); p.setDesiredLocation(c.getString(c.getColumnIndexOrThrow("desired_location"))); p.setWorkMode(c.getString(c.getColumnIndexOrThrow("work_mode"))); p.setYearsExperience(c.getString(c.getColumnIndexOrThrow("years_experience"))); p.setAvailableDate(c.getString(c.getColumnIndexOrThrow("available_date"))); return p;
        }
    }
    public boolean upsertCareerPreference(CareerPreference p) { ContentValues v=new ContentValues(); v.put("user_id",p.getUserId()); v.put("desired_position",p.getDesiredPosition()); v.put("category_id",p.getCategoryId()); v.put("career_level",p.getCareerLevel()); v.put("job_type",p.getJobType()); v.put("expected_salary",p.getExpectedSalary()); v.put("desired_location",p.getDesiredLocation()); v.put("work_mode",p.getWorkMode()); v.put("years_experience",p.getYearsExperience()); v.put("available_date",p.getAvailableDate()); return getWritableDatabase().insertWithOnConflict("career_preferences",null,v,SQLiteDatabase.CONFLICT_REPLACE)>0; }
    public ProfileSettings getProfileSettings(int userId) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM profile_settings WHERE user_id = ?", new String[]{String.valueOf(userId)})) {
            if (!cursor.moveToFirst()) return null;
            ProfileSettings settings = new ProfileSettings();
            int index = cursor.getColumnIndex("id");
            if (index >= 0) settings.id = cursor.getInt(index);
            settings.userId = userId;
            index = cursor.getColumnIndex("profile_visible");
            if (index >= 0) settings.profileVisible = cursor.getInt(index) == 1;
            index = cursor.getColumnIndex("show_phone");
            if (index >= 0) settings.showPhone = cursor.getInt(index) == 1;
            index = cursor.getColumnIndex("show_email");
            if (index >= 0) settings.showEmail = cursor.getInt(index) == 1;
            index = cursor.getColumnIndex("allow_job_invites");
            if (index >= 0) settings.allowJobInvites = cursor.getInt(index) == 1;
            return settings;
        }
    }

    public boolean upsertProfileSettings(ProfileSettings settings) {
        if (settings == null || settings.userId <= 0) return false;
        ContentValues values = new ContentValues();
        values.put("profile_visible", settings.profileVisible ? 1 : 0);
        values.put("show_phone", settings.showPhone ? 1 : 0);
        values.put("show_email", settings.showEmail ? 1 : 0);
        values.put("allow_job_invites", settings.allowJobInvites ? 1 : 0);
        SQLiteDatabase db = getWritableDatabase();
        if (db.update("profile_settings", values, "user_id = ?",
                new String[]{String.valueOf(settings.userId)}) > 0) return true;
        values.put("user_id", settings.userId);
        return db.insert("profile_settings", null, values) != -1;
    }
    public int getProfileCompletion(int userId) { User u=getUserById(userId); if(u==null)return 0; int done=0,total=8; if(u.getAvatar()!=null&&!u.getAvatar().trim().isEmpty())done++;if(u.getPhone()!=null&&!u.getPhone().trim().isEmpty())done++;if(u.getLocation()!=null&&!u.getLocation().trim().isEmpty())done++;if(u.getBio()!=null&&!u.getBio().trim().isEmpty())done++;if(u.getEducation()!=null&&!u.getEducation().trim().isEmpty())done++;if(!getSkills(userId).isEmpty())done++;if(!getExperiences(userId).isEmpty())done++;if(getCVByUserId(userId)!=null)done++;return done*100/total; }
    public List<Education> getEducations(int uid){List<Education> a=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM educations WHERE user_id=? ORDER BY id DESC",new String[]{String.valueOf(uid)})){while(c.moveToNext()){Education e=new Education();e.setId(c.getInt(c.getColumnIndexOrThrow("id")));e.setUserId(uid);e.setSchoolName(c.getString(c.getColumnIndexOrThrow("school_name")));e.setMajor(c.getString(c.getColumnIndexOrThrow("major")));e.setDegree(c.getString(c.getColumnIndexOrThrow("degree")));e.setStartDate(c.getString(c.getColumnIndexOrThrow("start_date")));e.setEndDate(c.getString(c.getColumnIndexOrThrow("end_date")));e.setGpa(c.getString(c.getColumnIndexOrThrow("gpa")));e.setDescription(c.getString(c.getColumnIndexOrThrow("description")));a.add(e);}}return a;}
    public Education getEducationById(int id,int uid){for(Education e:getEducations(uid))if(e.getId()==id)return e;return null;}
    public long addEducation(Education e){ContentValues v=new ContentValues();v.put("user_id",e.getUserId());v.put("school_name",e.getSchoolName());v.put("major",e.getMajor());v.put("degree",e.getDegree());v.put("start_date",e.getStartDate());v.put("end_date",e.getEndDate());v.put("gpa",e.getGpa());v.put("description",e.getDescription());return getWritableDatabase().insert("educations",null,v);}
    public boolean updateEducation(Education e){ContentValues v=new ContentValues();v.put("school_name",e.getSchoolName());v.put("major",e.getMajor());v.put("degree",e.getDegree());v.put("start_date",e.getStartDate());v.put("end_date",e.getEndDate());v.put("gpa",e.getGpa());v.put("description",e.getDescription());return getWritableDatabase().update("educations",v,"id=? AND user_id=?",new String[]{String.valueOf(e.getId()),String.valueOf(e.getUserId())})>0;}
    public boolean deleteEducation(int id,int uid){return getWritableDatabase().delete("educations","id=? AND user_id=?",new String[]{String.valueOf(id),String.valueOf(uid)})>0;}
    public boolean languageExists(int userId, String languageName) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT 1 FROM languages WHERE user_id = ? AND LOWER(language_name) = LOWER(?)",
                new String[]{String.valueOf(userId), languageName == null ? "" : languageName.trim()})) {
            return cursor.moveToFirst();
        }
    }

    public List<Project> getProjects(int userId) {
        List<Project> projects = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM projects WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)})) {
            while (cursor.moveToNext()) projects.add(projectFromCursor(cursor));
        }
        return projects;
    }

    public Project getProjectById(int projectId, int userId) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM projects WHERE id = ? AND user_id = ?",
                new String[]{String.valueOf(projectId), String.valueOf(userId)})) {
            return cursor.moveToFirst() ? projectFromCursor(cursor) : null;
        }
    }

    public long addProject(Project project) {
        if (project == null || project.getUserId() <= 0) return -1;
        return getWritableDatabase().insert("projects", null, projectValues(project, true));
    }

    public boolean updateProject(Project project) {
        return project != null && project.getId() > 0 && project.getUserId() > 0
                && getWritableDatabase().update("projects", projectValues(project, false), "id = ? AND user_id = ?",
                new String[]{String.valueOf(project.getId()), String.valueOf(project.getUserId())}) > 0;
    }

    public boolean deleteProject(int projectId, int userId) {
        return getWritableDatabase().delete("projects", "id = ? AND user_id = ?",
                new String[]{String.valueOf(projectId), String.valueOf(userId)}) > 0;
    }

    public List<Certificate> getCertificates(int userId) {
        List<Certificate> certificates = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM certificates WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)})) {
            while (cursor.moveToNext()) certificates.add(certificateFromCursor(cursor));
        }
        return certificates;
    }

    public Certificate getCertificateById(int certificateId, int userId) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM certificates WHERE id = ? AND user_id = ?",
                new String[]{String.valueOf(certificateId), String.valueOf(userId)})) {
            return cursor.moveToFirst() ? certificateFromCursor(cursor) : null;
        }
    }

    public long addCertificate(Certificate certificate) {
        if (certificate == null || certificate.getUserId() <= 0) return -1;
        return getWritableDatabase().insert("certificates", null, certificateValues(certificate, true));
    }

    public boolean updateCertificate(Certificate certificate) {
        return certificate != null && certificate.getId() > 0 && certificate.getUserId() > 0
                && getWritableDatabase().update("certificates", certificateValues(certificate, false), "id = ? AND user_id = ?",
                new String[]{String.valueOf(certificate.getId()), String.valueOf(certificate.getUserId())}) > 0;
    }

    public boolean deleteCertificate(int certificateId, int userId) {
        return getWritableDatabase().delete("certificates", "id = ? AND user_id = ?",
                new String[]{String.valueOf(certificateId), String.valueOf(userId)}) > 0;
    }

    public List<Language> getLanguages(int userId) {
        List<Language> languages = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM languages WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)})) {
            while (cursor.moveToNext()) languages.add(languageFromCursor(cursor));
        }
        return languages;
    }

    public Language getLanguageById(int languageId, int userId) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM languages WHERE id = ? AND user_id = ?",
                new String[]{String.valueOf(languageId), String.valueOf(userId)})) {
            return cursor.moveToFirst() ? languageFromCursor(cursor) : null;
        }
    }

    public long addLanguage(Language language) {
        if (language == null || language.getUserId() <= 0) return -1;
        return getWritableDatabase().insert("languages", null, languageValues(language, true));
    }

    public boolean updateLanguage(Language language) {
        return language != null && language.getId() > 0 && language.getUserId() > 0
                && getWritableDatabase().update("languages", languageValues(language, false), "id = ? AND user_id = ?",
                new String[]{String.valueOf(language.getId()), String.valueOf(language.getUserId())}) > 0;
    }

    public boolean deleteLanguage(int languageId, int userId) {
        return getWritableDatabase().delete("languages", "id = ? AND user_id = ?",
                new String[]{String.valueOf(languageId), String.valueOf(userId)}) > 0;
    }

    public SocialLinks getSocialLinks(int userId) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM social_links WHERE user_id = ?", new String[]{String.valueOf(userId)})) {
            if (!cursor.moveToFirst()) return null;
            SocialLinks links = new SocialLinks();
            int index = cursor.getColumnIndex("id"); if (index >= 0) links.setId(cursor.getInt(index));
            links.setUserId(userId);
            index = cursor.getColumnIndex("github_url"); if (index >= 0) links.setGithubUrl(cursor.getString(index));
            index = cursor.getColumnIndex("linkedin_url"); if (index >= 0) links.setLinkedinUrl(cursor.getString(index));
            index = cursor.getColumnIndex("portfolio_url"); if (index >= 0) links.setPortfolioUrl(cursor.getString(index));
            index = cursor.getColumnIndex("website_url"); if (index >= 0) links.setWebsiteUrl(cursor.getString(index));
            return links;
        }
    }

    public boolean upsertSocialLinks(SocialLinks links) {
        if (links == null || links.getUserId() <= 0) return false;
        ContentValues values = socialLinksValues(links, false);
        SQLiteDatabase db = getWritableDatabase();
        if (db.update("social_links", values, "user_id = ?", new String[]{String.valueOf(links.getUserId())}) > 0) return true;
        values.put("user_id", links.getUserId());
        return db.insert("social_links", null, values) != -1;
    }

    private ContentValues projectValues(Project project, boolean includeUserId) {
        ContentValues values = new ContentValues();
        if (includeUserId) values.put("user_id", project.getUserId());
        values.put("project_name", project.getProjectName()); values.put("role", project.getRole());
        values.put("description", project.getDescription()); values.put("technologies", project.getTechnologies());
        values.put("start_date", project.getStartDate()); values.put("end_date", project.getEndDate());
        values.put("github_url", project.getGithubUrl()); values.put("demo_url", project.getDemoUrl());
        return values;
    }

    private ContentValues certificateValues(Certificate certificate, boolean includeUserId) {
        ContentValues values = new ContentValues();
        if (includeUserId) values.put("user_id", certificate.getUserId());
        values.put("certificate_name", certificate.getCertificateName()); values.put("organization", certificate.getOrganization());
        values.put("issue_date", certificate.getIssueDate()); values.put("expiry_date", certificate.getExpiryDate());
        values.put("credential_id", certificate.getCredentialId()); values.put("credential_url", certificate.getCredentialUrl());
        return values;
    }

    private ContentValues languageValues(Language language, boolean includeUserId) {
        ContentValues values = new ContentValues();
        if (includeUserId) values.put("user_id", language.getUserId());
        values.put("language_name", language.getLanguageName()); values.put("proficiency", language.getProficiency());
        values.put("certificate", language.getCertificate()); return values;
    }

    private ContentValues socialLinksValues(SocialLinks links, boolean includeUserId) {
        ContentValues values = new ContentValues();
        if (includeUserId) values.put("user_id", links.getUserId());
        values.put("github_url", links.getGithubUrl()); values.put("linkedin_url", links.getLinkedinUrl());
        values.put("portfolio_url", links.getPortfolioUrl()); values.put("website_url", links.getWebsiteUrl());
        return values;
    }

    private Project projectFromCursor(Cursor cursor) {
        Project project = new Project();
        int index = cursor.getColumnIndex("id"); if (index >= 0) project.setId(cursor.getInt(index));
        index = cursor.getColumnIndex("user_id"); if (index >= 0) project.setUserId(cursor.getInt(index));
        index = cursor.getColumnIndex("project_name"); if (index >= 0) project.setProjectName(cursor.getString(index));
        index = cursor.getColumnIndex("role"); if (index >= 0) project.setRole(cursor.getString(index));
        index = cursor.getColumnIndex("description"); if (index >= 0) project.setDescription(cursor.getString(index));
        index = cursor.getColumnIndex("technologies"); if (index >= 0) project.setTechnologies(cursor.getString(index));
        index = cursor.getColumnIndex("start_date"); if (index >= 0) project.setStartDate(cursor.getString(index));
        index = cursor.getColumnIndex("end_date"); if (index >= 0) project.setEndDate(cursor.getString(index));
        index = cursor.getColumnIndex("github_url"); if (index >= 0) project.setGithubUrl(cursor.getString(index));
        index = cursor.getColumnIndex("demo_url"); if (index >= 0) project.setDemoUrl(cursor.getString(index));
        return project;
    }

    private Certificate certificateFromCursor(Cursor cursor) {
        Certificate certificate = new Certificate();
        int index = cursor.getColumnIndex("id"); if (index >= 0) certificate.setId(cursor.getInt(index));
        index = cursor.getColumnIndex("user_id"); if (index >= 0) certificate.setUserId(cursor.getInt(index));
        index = cursor.getColumnIndex("certificate_name"); if (index >= 0) certificate.setCertificateName(cursor.getString(index));
        index = cursor.getColumnIndex("organization"); if (index >= 0) certificate.setOrganization(cursor.getString(index));
        index = cursor.getColumnIndex("issue_date"); if (index >= 0) certificate.setIssueDate(cursor.getString(index));
        index = cursor.getColumnIndex("expiry_date"); if (index >= 0) certificate.setExpiryDate(cursor.getString(index));
        index = cursor.getColumnIndex("credential_id"); if (index >= 0) certificate.setCredentialId(cursor.getString(index));
        index = cursor.getColumnIndex("credential_url"); if (index >= 0) certificate.setCredentialUrl(cursor.getString(index));
        return certificate;
    }

    private Language languageFromCursor(Cursor cursor) {
        Language language = new Language();
        int index = cursor.getColumnIndex("id"); if (index >= 0) language.setId(cursor.getInt(index));
        index = cursor.getColumnIndex("user_id"); if (index >= 0) language.setUserId(cursor.getInt(index));
        index = cursor.getColumnIndex("language_name"); if (index >= 0) language.setLanguageName(cursor.getString(index));
        index = cursor.getColumnIndex("proficiency"); if (index >= 0) language.setProficiency(cursor.getString(index));
        index = cursor.getColumnIndex("certificate"); if (index >= 0) language.setCertificate(cursor.getString(index));
        return language;
    }
}
