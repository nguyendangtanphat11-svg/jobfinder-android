package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.JobAdapter;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.UserSession;
import com.example.jobfinderapp.models.Job;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private RecyclerView rvRecentJobs;
    private UserSession userSession;

    // Các View tương tác từ XML
    private TextView tvLoginRegisterHint, tvUserGreeting;
    private ImageView btnNotification;
    private BottomNavigationView bottomNavigation;
    private ExtendedFloatingActionButton fabSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userSession = new UserSession(this);

        // 1. Ánh xạ đúng chuẩn ID theo XML
        tvUserGreeting = findViewById(R.id.tvUserGreeting);
        tvLoginRegisterHint = findViewById(R.id.tvLoginRegisterHint); // Nút Đăng nhập / Đăng ký
        btnNotification = findViewById(R.id.btnNotification);          // Nút Thông báo
        bottomNavigation = findViewById(R.id.bottomNavigation);        // Thanh Menu Đáy
        fabSupport = findViewById(R.id.fabSupport);                    // Nút Hỗ trợ nổi

        rvCategories = findViewById(R.id.rvCategories);
        rvRecentJobs = findViewById(R.id.rvJobs);

        // 2. Cài đặt hiển thị thông tin Đăng nhập ở Header
        setupHeaderUI();

        // 3. Bắt sự kiện Click nút "Đăng nhập / Đăng ký ngay"
        if (tvLoginRegisterHint != null) {
            tvLoginRegisterHint.setOnClickListener(v -> {
                if (!userSession.isLoggedIn()) {
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 4. Bắt sự kiện Click nút Thông báo (Biểu tượng Quả chuông)
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> checkAuthAndNavigate(NotificationActivity.class));
        }

        // 5. Bắt sự kiện Click nút Extended FAB "Hỗ trợ 💬"
        if (fabSupport != null) {
            fabSupport.setOnClickListener(v -> {
                Toast.makeText(HomeActivity.this, "Chức năng hỗ trợ trực tuyến đang phát triển!", Toast.LENGTH_SHORT).show();
            });
        }

        // 6. Xử lý Chuyển trang khi bấm Thanh Điều Hướng Đáy (BottomNavigationView)
        setupBottomNavigation();

        // 7. Khởi tạo danh sách công việc & ngành nghề
        setupRecyclerViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại giao diện Header khi quay lại từ màn hình Đăng nhập
        setupHeaderUI();
    }

    private void setupHeaderUI() {
        if (userSession.isLoggedIn()) {
            if (tvUserGreeting != null) tvUserGreeting.setText("Xin chào bạn! 👋");
            if (tvLoginRegisterHint != null) {
                tvLoginRegisterHint.setText("Đã đăng nhập");
                tvLoginRegisterHint.setEnabled(false); // Đã đăng nhập thì ẩn click
            }
        } else {
            if (tvUserGreeting != null) tvUserGreeting.setText("Xin chào, Sinh viên! 👋");
            if (tvLoginRegisterHint != null) {
                tvLoginRegisterHint.setText("Đăng nhập / Đăng ký ngay");
                tvLoginRegisterHint.setEnabled(true);
            }
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation != null) {
            // Đánh dấu tab Home luôn được chọn
            bottomNavigation.setSelectedItemId(R.id.nav_home);

            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_favorite) { // Kiểm tra ID này khớp trong res/menu/bottom_nav_menu.xml
                    checkAuthAndNavigate(FavoriteActivity.class);
                    return true;
                } else if (itemId == R.id.nav_history) {  // Tab lịch sử ứng tuyển
                    checkAuthAndNavigate(ApplicationHistoryActivity.class);
                    return true;
                } else if (itemId == R.id.nav_profile) {  // Tab cá nhân
                    checkAuthAndNavigate(ProfileActivity.class);
                    return true;
                }
                return false;
            });
        }
    }

    private void setupRecyclerViews() {
        if (rvRecentJobs != null) {
            rvRecentJobs.setLayoutManager(new LinearLayoutManager(this));
            // Tránh việc RecyclerView bị giật lag khi nằm trong NestedScrollView
            rvRecentJobs.setNestedScrollingEnabled(false);

            List<Job> jobList = new ArrayList<>();
            jobList.add(new Job("Android Developer Intern", "Công ty Công nghệ ABC", "Quận 1, HCM", "Thỏa thuận", "Thực tập"));
            jobList.add(new Job("Java Backend Intern", "Tập đoàn Giải pháp XYZ", "Bình Thạnh, HCM", "5 - 7 Triệu", "Toàn thời gian"));
            jobList.add(new Job("UI/UX Design Intern", "Ví Điện Tử MoMo", "Quận 3, HCM", "Thỏa thuận", "Thực tập"));

            JobAdapter jobAdapter = new JobAdapter(this, jobList);
            rvRecentJobs.setAdapter(jobAdapter);
        }

        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }
    }

    public void checkAuthAndNavigate(Class<?> targetActivity) {
        if (userSession != null && userSession.isLoggedIn()) {
            Intent intent = new Intent(HomeActivity.this, targetActivity);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
}