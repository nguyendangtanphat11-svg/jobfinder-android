package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.JobAdapter;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.UserSession;
import com.example.jobfinderapp.models.Job;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private RecyclerView rvRecentJobs;
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo UserSession
        userSession = new UserSession(this);

        // Ánh xạ RecyclerView an toàn
        rvCategories = findViewById(R.id.rvCategories);
        rvRecentJobs = findViewById(R.id.rvJobs);

        // 1. Cài đặt Danh sách Việc làm chính (rvJobs)
        if (rvRecentJobs != null) {
            rvRecentJobs.setLayoutManager(new LinearLayoutManager(this));

            List<Job> jobList = new ArrayList<>();
            jobList.add(new Job("Android Developer Intern", "Công ty Công nghệ ABC", "Quận 1, HCM", "Thỏa thuận", "Thực tập"));
            jobList.add(new Job("Java Backend Intern", "Tập đoàn Giải pháp XYZ", "Bình Thạnh, HCM", "5 - 7 Triệu", "Toàn thời gian"));
            jobList.add(new Job("UI/UX Design Intern", "Ví Điện Tử MoMo", "Quận 3, HCM", "Thỏa thuận", "Thực tập"));
            jobList.add(new Job("Frontend Web Intern", "VNG Campus", "Quận 7, HCM", "Lương cạnh tranh", "Toàn thời gian"));
            jobList.add(new Job("Flutter Mobile Intern", "FPT Software", "Thủ Đức, HCM", "4 - 6 Triệu", "Thực tập"));

            // Gọi Constructor 4 tham số chuẩn của bạn
            // Ép kiểu null về Object Listener để Java nhận biết đúng Constructor
            JobAdapter jobAdapter = new JobAdapter(this, jobList, -1, (JobAdapter.OnFavoriteClickListener) null);
            rvRecentJobs.setAdapter(jobAdapter);
        }

        // 2. Cài đặt Danh mục / Nổi bật (rvCategories)
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