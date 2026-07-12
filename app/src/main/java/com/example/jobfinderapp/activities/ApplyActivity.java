package com.example.jobfinderapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.jobfinderapp.models.Job;
import com.example.jobfinderapp.models.User;
import com.google.android.material.button.MaterialButton;

public class ApplyActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivApplyLogo;
    private TextView tvApplyJobTitle, tvApplyCompanyName, tvApplySalary, tvApplyLocation;
    private TextView tvApplyUserName, tvApplyUserEmail, tvApplyUserPhone, tvCVName;
    private MaterialButton btnSelectCV, btnSubmitApply;

    private DBHelper dbHelper;
    private int jobId;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_apply);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DBHelper(this);
        jobId = getIntent().getIntExtra("JOB_ID", -1);
        
        // Lấy userId từ SharedPreferences
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = pref.getInt("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để ứng tuyển", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadData();
        setupEvents();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivApplyLogo = findViewById(R.id.ivApplyLogo);
        tvApplyJobTitle = findViewById(R.id.tvApplyJobTitle);
        tvApplyCompanyName = findViewById(R.id.tvApplyCompanyName);
        tvApplySalary = findViewById(R.id.tvApplySalary);
        tvApplyLocation = findViewById(R.id.tvApplyLocation);
        
        tvApplyUserName = findViewById(R.id.tvApplyUserName);
        tvApplyUserEmail = findViewById(R.id.tvApplyUserEmail);
        tvApplyUserPhone = findViewById(R.id.tvApplyUserPhone);
        tvCVName = findViewById(R.id.tvCVName);
        
        btnSelectCV = findViewById(R.id.btnSelectCV);
        btnSubmitApply = findViewById(R.id.btnSubmitApply);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ứng tuyển công việc");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadData() {
        if (jobId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID công việc", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load Job Info
        Job job = dbHelper.getJobById(jobId);
        if (job != null) {
            tvApplyJobTitle.setText(job.getTitle());
            tvApplyCompanyName.setText(job.getCompanyName());
            tvApplySalary.setText(job.getSalary());
            tvApplyLocation.setText(job.getLocation());
            Glide.with(this)
                    .load(job.getCompanyLogo())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(ivApplyLogo);
        }

        // Load User Info
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            tvApplyUserName.setText(user.getFullname());
            tvApplyUserEmail.setText(user.getEmail());
            tvApplyUserPhone.setText(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "Chưa cập nhật");
        }

        // Load CV Info
        String cv = dbHelper.getCVNameByUserId(userId);
        tvCVName.setText(cv);
    }

    private void setupEvents() {
        btnSelectCV.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng chọn CV từ thiết bị đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        btnSubmitApply.setOnClickListener(v -> {
            if (dbHelper.isApplied(userId, jobId)) {
                Toast.makeText(this, "Bạn đã ứng tuyển công việc này.", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = dbHelper.applyJob(userId, jobId);
                if (success) {
                    Job job = dbHelper.getJobById(jobId);
                    String jobTitle = (job != null) ? job.getTitle() : "vị trí mới";
                    String companyName = (job != null) ? job.getCompanyName() : "công ty";
                    
                    String notifTitle = "Ứng tuyển thành công";
                    String notifContent = "Bạn đã ứng tuyển thành công vào vị trí " + jobTitle + " tại " + companyName + ".";
                    dbHelper.addNotification(userId, notifTitle, notifContent);
                    
                    Toast.makeText(this, "Ứng tuyển thành công.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
