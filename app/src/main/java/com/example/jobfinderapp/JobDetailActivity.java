package com.example.jobfinderapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.jobfinderapp.R;

public class JobDetailActivity extends AppCompatActivity {

    private TextView tvJobName, tvCompanyName, tvJobSalary, tvJobAddress, tvJobGeneralInfo, tvJobTime, tvJobDescription, tvJobRequirement, tvJobBenefits;
    private Button btnUserAccountStatus, btnApplyNow;
    private ImageButton btnFavorite;

    private boolean isSaved = false; // Trạng thái lưu việc làm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        // 1. Ánh xạ các View từ layout XML
        initViews();

        // 2. Xử lý trạng thái tài khoản Đăng nhập trên thanh Header
        setupAccountHeader();

        // 3. Xử lý khi bấm nút "Lưu việc" (Đã sửa lỗi android.R.drawable hoàn toàn)
        btnFavorite.setOnClickListener(v -> {
            isSaved = !isSaved;
            if (isSaved) {
                btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                Toast.makeText(this, "Đã lưu việc làm vào danh sách yêu thích!", Toast.LENGTH_SHORT).show();
            } else {
                btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                Toast.makeText(this, "Đã bỏ lưu việc làm!", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Xử lý khi bấm nút "Ứng tuyển ngay" -> Chuyển sang ApplyActivity
        btnApplyNow.setOnClickListener(v -> {
            Intent intent = new Intent(JobDetailActivity.this, ApplyActivity.class);
            intent.putExtra("JOB_NAME_KEY", tvJobName.getText().toString());
            startActivity(intent);
        });
    }

    private void initViews() {
        tvJobName = findViewById(R.id.tvJobName);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvJobSalary = findViewById(R.id.tvJobSalary);
        tvJobGeneralInfo = findViewById(R.id.tvJobGeneralInfo);
        tvJobAddress = findViewById(R.id.tvJobAddress);
        tvJobTime = findViewById(R.id.tvJobTime);
        tvJobDescription = findViewById(R.id.tvJobDescription);
        tvJobRequirement = findViewById(R.id.tvJobRequirement);
        tvJobBenefits = findViewById(R.id.tvJobBenefits);

        btnUserAccountStatus = findViewById(R.id.btnUserAccountStatus);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnApplyNow = findViewById(R.id.btnApplyNow);
    }

    private void setupAccountHeader() {
        // Giả lập trạng thái tài khoản (Khi gộp code sẽ kết nối với phần đăng nhập của Người 2)
        boolean isUserLoggedIn = false;
        String username = "Tuấn Kiệt";

        if (isUserLoggedIn) {
            btnUserAccountStatus.setText(username);
        } else {
            btnUserAccountStatus.setText("Đăng nhập");
            btnUserAccountStatus.setOnClickListener(v -> {
                Toast.makeText(this, "Chuyển hướng đến màn hình Đăng nhập...", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(this, LoginActivity.class);
                // startActivity(intent);
            });
        }
    }
}