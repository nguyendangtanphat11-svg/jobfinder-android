package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.jobfinderapp.R;
import com.google.android.material.button.MaterialButton;

public class JobDetailActivity extends AppCompatActivity {

    // 1. Khai báo biến employerPhone ở đây để dùng chung cho toàn bộ Class
    private String employerPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        // Toolbar nút Back
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Ánh xạ View
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvCompany = findViewById(R.id.tvDetailCompany);
        TextView tvSalary = findViewById(R.id.tvDetailSalary);
        TextView tvLocation = findViewById(R.id.tvDetailLocation);
        MaterialButton btnApply = findViewById(R.id.btnApply);
        MaterialButton btnContact = findViewById(R.id.btnContact);

        // Lấy dữ liệu Intent
        Intent intent = getIntent();
        if (intent != null) {
            if (tvTitle != null) tvTitle.setText(intent.getStringExtra("JOB_TITLE"));
            if (tvCompany != null) tvCompany.setText(intent.getStringExtra("JOB_COMPANY"));
            if (tvSalary != null) tvSalary.setText("💵 Mức lương: " + intent.getStringExtra("JOB_SALARY"));
            if (tvLocation != null) tvLocation.setText("📍 Địa điểm: " + intent.getStringExtra("JOB_LOCATION"));

            // Gán giá trị SĐT vào biến đã khai báo
            employerPhone = intent.getStringExtra("EMPLOYER_PHONE");
        }

        // 2. Xử lý nút Ứng tuyển
        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                // Sửa tham số Toast truyền vào JobDetailActivity.this
                Toast.makeText(JobDetailActivity.this, "Nộp hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                btnApply.setText("Đã ứng tuyển");
                btnApply.setEnabled(false);
            });
        }

        // 3. Xử lý nút Liên hệ (Mở trình gọi điện)
        if (btnContact != null) {
            btnContact.setOnClickListener(v -> {
                if (employerPhone != null && !employerPhone.isEmpty()) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + employerPhone));
                    startActivity(callIntent);
                } else {
                    Toast.makeText(JobDetailActivity.this, "Không tìm thấy số điện thoại NTD", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}