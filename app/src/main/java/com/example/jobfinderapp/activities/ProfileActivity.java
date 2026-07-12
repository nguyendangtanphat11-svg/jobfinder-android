package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.CV;
import com.example.jobfinderapp.models.Company;
import com.example.jobfinderapp.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextView tvFullname, tvEmail, tvPhone;
    private TextView tvLabel1, tvValue1, tvLabel2, tvValue2, tvLabel3, tvValue3, tvLabel4, tvValue4;
    private LinearLayout layoutCandidateActions;
    private MaterialButton btnEditProfile, btnLogout;
    private MaterialCardView btnHistory, btnFavorite;
    private DBHelper dbHelper;
    private int currentUserId;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DBHelper(this);
        initViews();
        loadSession();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        tvFullname = findViewById(R.id.tvFullname);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        
        layoutCandidateActions = findViewById(R.id.layoutCandidateActions);
        btnHistory = findViewById(R.id.btnHistory);
        btnFavorite = findViewById(R.id.btnFavorite);
        
        tvLabel1 = findViewById(R.id.tvLabel1);
        tvValue1 = findViewById(R.id.tvValue1);
        tvLabel2 = findViewById(R.id.tvLabel2);
        tvValue2 = findViewById(R.id.tvValue2);
        tvLabel3 = findViewById(R.id.tvLabel3);
        tvValue3 = findViewById(R.id.tvValue3);
        tvLabel4 = findViewById(R.id.tvLabel4);
        tvValue4 = findViewById(R.id.tvValue4);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadSession() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = pref.getInt("USER_ID", -1);
        userRole = pref.getString("USER_ROLE", "candidate");

        if (currentUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void loadProfileData() {
        User user = dbHelper.getUserById(currentUserId);
        if (user == null) return;

        tvFullname.setText(user.getFullname());
        tvEmail.setText(user.getEmail());
        tvPhone.setText(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "Chưa cập nhật");

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Glide.with(this).load(user.getAvatar()).placeholder(R.drawable.ic_launcher_background).into(ivAvatar);
        }

        if ("employer".equals(userRole)) {
            layoutCandidateActions.setVisibility(View.GONE);
            loadEmployerData();
        } else {
            layoutCandidateActions.setVisibility(View.VISIBLE);
            loadCandidateData();
        }
    }

    private void loadEmployerData() {
        Company company = dbHelper.getCompanyByUserId(currentUserId);
        tvLabel1.setText("Tên công ty");
        tvLabel2.setText("Địa chỉ");
        tvLabel3.setText("Website");
        tvLabel4.setText("Mô tả");

        if (company != null) {
            tvValue1.setText(company.getName());
            tvValue2.setText(company.getAddress() != null && !company.getAddress().isEmpty() ? company.getAddress() : "Chưa cập nhật");
            tvValue3.setText(company.getWebsite() != null && !company.getWebsite().isEmpty() ? company.getWebsite() : "Chưa cập nhật");
            tvValue4.setText(company.getDescription() != null && !company.getDescription().isEmpty() ? company.getDescription() : "Chưa cập nhật");
        }
    }

    private void loadCandidateData() {
        CV cv = dbHelper.getCVByUserId(currentUserId);
        tvLabel1.setText("Mục tiêu nghề nghiệp");
        tvLabel2.setText("Kỹ năng");
        tvLabel3.setText("Học vấn");
        tvLabel4.setText("Kinh nghiệm");

        if (cv != null) {
            tvValue1.setText(cv.getObjective() != null && !cv.getObjective().isEmpty() ? cv.getObjective() : "Chưa cập nhật");
            tvValue2.setText(cv.getSkills() != null && !cv.getSkills().isEmpty() ? cv.getSkills() : "Chưa cập nhật");
            tvValue3.setText(cv.getEducation() != null && !cv.getEducation().isEmpty() ? cv.getEducation() : "Chưa cập nhật");
            tvValue4.setText(cv.getExperience() != null && !cv.getExperience().isEmpty() ? cv.getExperience() : "Chưa cập nhật");
        }
    }

    private void setupEvents() {
        btnEditProfile.setOnClickListener(v -> {
            if ("employer".equals(userRole)) {
                startActivity(new Intent(this, CompanyProfileActivity.class));
            } else {
                Intent intent = new Intent(this, EditProfileActivity.class);
                intent.putExtra("USER_ID", currentUserId);
                startActivity(intent);
            }
        });

        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, ApplicationHistoryActivity.class)));
        btnFavorite.setOnClickListener(v -> startActivity(new Intent(this, FavoriteActivity.class)));

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }
}
