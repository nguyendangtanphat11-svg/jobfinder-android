package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.adapters.CategoryAdapter;
import com.example.jobfinderapp.adapters.JobAdapter;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Category;
import com.example.jobfinderapp.models.Job;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView tvUserName;
    private ImageView ivNotification, ivUserAvatar;
    private EditText etSearch;
    private RecyclerView rvCategories, rvJobs;
    private BottomNavigationView bottomNavigation;
    
    private DBHelper dbHelper;
    private JobAdapter jobAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Job> jobList;
    private List<Category> categoryList;
    
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra quyền truy cập (Candidate only)
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String role = pref.getString("USER_ROLE", "candidate");
        if (!"candidate".equals(role)) {
            Toast.makeText(this, "Bạn không có quyền truy cập trang này", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        dbHelper = new DBHelper(this);
        initViews();
        setupUserData();
        setupCategories();
        setupJobs();
        setupBottomNavigation();
        setupEvents();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        ivNotification = findViewById(R.id.ivNotification);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        etSearch = findViewById(R.id.etSearch);
        rvCategories = findViewById(R.id.rvCategories);
        rvJobs = findViewById(R.id.rvJobs);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupUserData() {
        // Lấy thông tin từ SharedPreferences
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = pref.getInt("USER_ID", -1);
        String userName = pref.getString("USER_NAME", "Người dùng");
        
        if (currentUserId == -1) {
            // Nếu chưa login, quay lại LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        tvUserName.setText(userName);
    }

    private void setupCategories() {
        categoryList = dbHelper.getAllCategories();
        categoryAdapter = new CategoryAdapter(categoryList);
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupJobs() {
        jobList = dbHelper.getAllJobs();
        jobAdapter = new JobAdapter(this, jobList, currentUserId, (job, isFavorite) -> {
            // Xử lý toggle Favorite
            if (isFavorite) {
                dbHelper.removeFavorite(currentUserId, job.getId());
                Toast.makeText(HomeActivity.this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addToFavorite(currentUserId, job.getId());
                Toast.makeText(HomeActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        rvJobs.setAdapter(jobAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_favorite) {
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupEvents() {
        // Chuyển sang NotificationActivity
        ivNotification.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        // Xử lý tìm kiếm truy vấn trực tiếp SQLite
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                List<Job> filteredJobs = dbHelper.searchJobs(keyword);
                jobAdapter.updateList(filteredJobs);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
