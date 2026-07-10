package com.example.jobfinderapp.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import com.example.jobfinderapp.JobAdapter;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.models.Job;

public class HomeActivity extends AppCompatActivity {

    // Khai báo biến toàn cục để tránh lỗi đỏ "Cannot resolve symbol"
    private RecyclerView rvFeaturedJobs;
    private RecyclerView rvRecentJobs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Ánh xạ View từ XML layout
        rvFeaturedJobs = findViewById(R.id.rvFeaturedJobs);
        rvRecentJobs = findViewById(R.id.rvRecentJobs);

        // Cài đặt LayoutManager hiển thị dạng danh sách cuộn dọc
        rvFeaturedJobs.setLayoutManager(new LinearLayoutManager(this));
        rvRecentJobs.setLayoutManager(new LinearLayoutManager(this));

        // DANH SÁCH VIỆC LÀM NỔI BẬT (Đã thêm link URL ảnh demo tại tham số cuối)
        List<Job> featuredJobsList = new ArrayList<>();
        featuredJobsList.add(new Job("Android Developer Intern", "Công ty Công nghệ ABC", "💰 Thỏa thuận", "📍 Quận 1, HCM", "https://picsum.photos/200?random=1"));
        featuredJobsList.add(new Job("Java Backend Intern", "Tập đoàn Giải pháp XYZ", "💰 5 - 7 Triệu", "📍 Bình Thạnh, HCM", "https://picsum.photos/200?random=2"));
        featuredJobsList.add(new Job("UI/UX Design Intern", "Ví Điện Tử MoMo", "💰 Thỏa thuận", "📍 Quận 3, HCM", "https://picsum.photos/200?random=3"));

        JobAdapter featuredAdapter = new JobAdapter(featuredJobsList);
        rvFeaturedJobs.setAdapter(featuredAdapter);

        // DANH SÁCH VIỆC LÀM MỚI NHẤT (Đã thêm link URL ảnh demo tại tham số cuối)
        List<Job> recentJobsList = new ArrayList<>();
        recentJobsList.add(new Job("Frontend Web Intern", "VNG Campus", "💰 Lương cạnh tranh", "📍 Quận 7, HCM", "https://picsum.photos/200?random=4"));
        recentJobsList.add(new Job("Flutter Mobile Intern", "FPT Software", "💰 4 - 6 Triệu", "📍 Thủ Đức, HCM", "https://picsum.photos/200?random=5"));
        recentJobsList.add(new Job("Python Data Trainee", "TMA Solutions", "💰 5 Triệu", "📍 Phú Nhuận, HCM", "https://picsum.photos/200?random=6"));
        recentJobsList.add(new Job("iOS Developer Intern", "VCCorp Group", "💰 Thỏa thuận", "📍 Cầu Giấy, HN", "https://picsum.photos/200?random=7"));

        JobAdapter recentAdapter = new JobAdapter(recentJobsList);
        rvRecentJobs.setAdapter(recentAdapter);
    }
}