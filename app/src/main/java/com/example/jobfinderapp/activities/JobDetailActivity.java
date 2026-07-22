package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.database.UserSession;
import com.example.jobfinderapp.models.Job;
import com.google.android.material.button.MaterialButton;

public class JobDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivDetailLogo;
    private TextView tvDetailTitle, tvDetailCompany, tvDetailSalary, tvDetailLocation, tvDetailDeadline, tvDetailDesc, tvDetailReq;
    private MaterialButton btnDetailFavorite, btnApplyNow;

    private DBHelper dbHelper;
    private UserSession userSession;
    private int jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_job_detail);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Khởi tạo Database & Session
        dbHelper = new DBHelper(this);
        userSession = new UserSession(this);

        jobId = getIntent().getIntExtra("JOB_ID", -1);

        initViews();
        loadJobDetails();
        setupEvents();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivDetailLogo = findViewById(R.id.ivDetailLogo);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailCompany = findViewById(R.id.tvDetailCompany);
        tvDetailSalary = findViewById(R.id.tvDetailSalary);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailDeadline = findViewById(R.id.tvDetailDeadline);
        tvDetailDesc = findViewById(R.id.tvDetailDesc);
        tvDetailReq = findViewById(R.id.tvDetailReq);
        btnDetailFavorite = findViewById(R.id.btnDetailFavorite);
        btnApplyNow = findViewById(R.id.btnApplyNow);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết công việc");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadJobDetails() {
        if (jobId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin công việc", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Job job = dbHelper.getJobById(jobId);
        if (job != null) {
            tvDetailTitle.setText(job.getTitle());
            tvDetailCompany.setText(job.getCompanyName());
            tvDetailSalary.setText(job.getSalary());
            tvDetailLocation.setText(job.getLocation());
            tvDetailDeadline.setText(job.getDeadline());
            tvDetailDesc.setText(job.getDescription());
            tvDetailReq.setText(job.getRequirement());

            Glide.with(this)
                    .load(job.getCompanyLogo())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(ivDetailLogo);

            updateFavoriteButton();
        }
    }

    private void updateFavoriteButton() {
        // Chỉ cập nhật trạng thái Yêu thích nếu người dùng đã đăng nhập
        if (userSession.isLoggedIn() && dbHelper.isFavorite(getCurrentUserId(), jobId)) {
            btnDetailFavorite.setIconResource(android.R.drawable.btn_star_big_on);
            btnDetailFavorite.setText("Đã yêu thích");
        } else {
            btnDetailFavorite.setIconResource(android.R.drawable.btn_star_big_off);
            btnDetailFavorite.setText("Yêu thích");
        }
    }

    private void setupEvents() {
        // 1. Sự kiện bấm Nút Yêu thích
        btnDetailFavorite.setOnClickListener(v -> {
            if (!userSession.isLoggedIn()) {
                redirectToLogin("Vui lòng đăng nhập để lưu công việc yêu thích!");
                return;
            }

            int currentUserId = getCurrentUserId();
            boolean isFav = dbHelper.isFavorite(currentUserId, jobId);
            boolean success = dbHelper.toggleFavorite(currentUserId, jobId);
            if (success) {
                updateFavoriteButton();
                if (!isFav) {
                    Job job = dbHelper.getJobById(jobId);
                    String jobTitle = (job != null) ? job.getTitle() : "công việc";
                    dbHelper.addNotification(currentUserId, "Đã thêm vào yêu thích",
                            "Đã thêm " + jobTitle + " vào danh sách yêu thích.");
                    Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 2. Sự kiện bấm Nút Ứng tuyển ngay
        btnApplyNow.setOnClickListener(v -> {
            if (!userSession.isLoggedIn()) {
                redirectToLogin("Vui lòng đăng nhập để ứng tuyển!");
                return;
            }

            Intent intent = new Intent(JobDetailActivity.this, ApplyActivity.class);
            intent.putExtra("JOB_ID", jobId);
            startActivity(intent);
        });
    }

    // Hàm tiện ích chuyển sang màn hình Đăng nhập
    private void redirectToLogin(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(JobDetailActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    // Hàm hỗ trợ lấy ID người dùng từ SharedPreferences
    private int getCurrentUserId() {
        return getSharedPreferences("UserSessionPref", MODE_PRIVATE).getInt("UserId", -1);
    }
}