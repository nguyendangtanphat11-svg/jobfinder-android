package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Company;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Map;

public class EmployerDashboardActivity extends AppCompatActivity {

    private TextView tvCompanyName, tvTotalJobs, tvTotalApplicants, tvActiveJobs;
    private MaterialCardView btnManageJobs, btnManageApplicants, btnCompanyProfile;
    private MaterialButton btnLogout;
    private DBHelper dbHelper;
    private int currentUserId;
    private Company company;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra quyền truy cập (Employer only)
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String role = pref.getString("USER_ROLE", "");
        if (!"employer".equals(role)) {
            Toast.makeText(this, "Bạn không có quyền truy cập trang này", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_employer_dashboard);

        dbHelper = new DBHelper(this);
        currentUserId = pref.getInt("USER_ID", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupEvents();
        loadDashboardData();
    }

    private void initViews() {
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvTotalJobs = findViewById(R.id.tvTotalJobs);
        tvTotalApplicants = findViewById(R.id.tvTotalApplicants);
        tvActiveJobs = findViewById(R.id.tvActiveJobs);
        btnManageJobs = findViewById(R.id.btnManageJobs);
        btnManageApplicants = findViewById(R.id.btnManageApplicants);
        btnCompanyProfile = findViewById(R.id.btnCompanyProfile);
        btnLogout = findViewById(R.id.btnLogoutEmployer);
    }

    private void setupEvents() {
        btnManageJobs.setOnClickListener(v -> startActivity(new Intent(this, ManageJobsActivity.class)));
        btnManageApplicants.setOnClickListener(v -> startActivity(new Intent(this, ManageApplicantsActivity.class)));
        btnCompanyProfile.setOnClickListener(v -> startActivity(new Intent(this, CompanyProfileActivity.class)));
        btnLogout.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
            pref.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void loadDashboardData() {
        company = dbHelper.getCompanyByUserId(currentUserId);
        if (company != null) {
            tvCompanyName.setText(company.getName());
            Map<String, Integer> stats = dbHelper.getEmployerStats(company.getId());
            tvTotalJobs.setText(String.valueOf(stats.getOrDefault("total_jobs", 0)));
            tvTotalApplicants.setText(String.valueOf(stats.getOrDefault("total_applicants", 0)));
            tvActiveJobs.setText(String.valueOf(stats.getOrDefault("active_jobs", 0)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }
}
