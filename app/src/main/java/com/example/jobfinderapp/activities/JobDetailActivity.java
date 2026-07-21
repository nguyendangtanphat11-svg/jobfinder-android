package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Job;

public class JobDetailActivity extends AppCompatActivity {

    // 1. Khai báo các thành phần giao diện (Views)
    private ImageButton btnBack, btnFavorite;
    private Button btnApplyNow;
    private TextView tvJobName, tvCompanyName, tvJobSalary, tvJobGeneralInfo,
            tvJobAddress, tvJobTime, tvJobDescription, tvJobRequirement, tvJobBenefits;

    // 2. Khai báo Database và các biến bổ trợ luồng dữ liệu
    private DBHelper dbHelper;
    private int jobId = -1;
    private int currentUserId = 2; // Giả định ID ứng viên mẫu "Nguyễn Văn A" trong hệ thống database là 2
    private boolean isSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        initViews();
        dbHelper = new DBHelper(this);

        // BƯỚC ĐỒNG BỘ: Lấy đúng ID người dùng đang đăng nhập giống như FavoriteActivity
        android.content.SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = pref.getInt("USER_ID", -1);

        // MẸO TEST: Nếu chưa làm chức năng Đăng nhập (ID trả về -1),
        // ta ép tạm bằng 2 (Ứng viên Nguyễn Văn A có sẵn trong DBHelper) để chạy thử không bị lỗi.
        if (currentUserId == -1) {
            currentUserId = 2;
        }

        // Nhận đúng nhãn JOB_ID từ JobAdapter của Phú gửi sang
        jobId = getIntent().getIntExtra("JOB_ID", -1);

        if (jobId != -1) {
            loadJobDetails(jobId);
            checkFavoriteStatus();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin công việc phù hợp!", Toast.LENGTH_SHORT).show();
        }

        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnApplyNow = findViewById(R.id.btnApplyNow);

        tvJobName = findViewById(R.id.tvJobName);
        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvJobSalary = findViewById(R.id.tvJobSalary);
        tvJobGeneralInfo = findViewById(R.id.tvJobGeneralInfo);
        tvJobAddress = findViewById(R.id.tvJobAddress); // Đã sửa từ tvJobLocation thành tvJobAddress khớp XML của Kiệt
        tvJobTime = findViewById(R.id.tvJobTime);
        tvJobDescription = findViewById(R.id.tvJobDescription);
        tvJobRequirement = findViewById(R.id.tvJobRequirement);
        tvJobBenefits = findViewById(R.id.tvJobBenefits);
    }

    private void loadJobDetails(int id) {
        // Truy vấn dữ liệu thực tế từ bảng Jobs thông qua DBHelper
        Job job = dbHelper.getJobById(id);

        if (job != null) {
            // Đổ dữ liệu động lấy từ Database lên các thẻ giao diện
            tvJobName.setText(job.getTitle());
            tvCompanyName.setText(job.getCompanyName()); // Đã sửa khớp hàm getCompanyName() trong Job.java
            tvJobSalary.setText(job.getSalary());
            tvJobAddress.setText(job.getLocation()); // Lấy cột Location đổ vào TextView địa chỉ

            // Đổ tiếp dữ liệu văn bản cho các phần Mô tả & Yêu cầu công việc từ kho DB
            if (job.getDescription() != null && !job.getDescription().isEmpty()) {
                tvJobDescription.setText(job.getDescription());
            }
            if (job.getRequirement() != null && !job.getRequirement().isEmpty()) {
                tvJobRequirement.setText(job.getRequirement());
            }

            // Bạn có thể giữ nguyên text mặc định trong XML cho các thẻ Thông tin chung / Thời gian / Quyền lợi
            // nếu cấu trúc database của nhóm chưa hỗ trợ các cột này.
        }
    }

    private void checkFavoriteStatus() {
        // Gọi hàm isFavorite từ DBHelper của Leader để kiểm tra trạng thái lưu
        isSaved = dbHelper.isFavorite(currentUserId, jobId);
        if (isSaved) {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_on); // Đổi icon thành Sao Vàng
        } else {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_off); // Đổi icon thành Sao Rỗng
        }
    }

    private void setupClickListeners() {
        // 1. Xử lý nút Mũi tên quay lại trang chủ (Góc trái trên cùng)
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng màn hình chi tiết hiện tại để quay về HomeActivity
            }
        });

        // 2. Xử lý nút Ngôi sao Yêu thích (Đồng bộ trực tiếp SQLite cho màn hình FavoriteActivity)
        // Xử lý nút Ngôi sao Yêu thích
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jobId == -1) {
                    Toast.makeText(JobDetailActivity.this, "Không có ID công việc hợp lệ!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Gọi hàm toggle của nhóm viết trong DBHelper
                boolean result = dbHelper.toggleFavorite(currentUserId, jobId);

                if (result) {
                    isSaved = !isSaved; // Đảo trạng thái hiển thị
                    if (isSaved) {
                        btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                        Toast.makeText(JobDetailActivity.this, "⭐ Đã thêm vào danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                    } else {
                        btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                        Toast.makeText(JobDetailActivity.this, "❌ Đã xóa khỏi danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(JobDetailActivity.this, "Thao tác thất bại, kiểm tra lại dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 3. Xử lý nút ỨNG TUYỂN NGAY chuyển tiếp sang màn hình hồ sơ ApplyActivity
        // Xử lý nút ỨNG TUYỂN NGAY chuyển sang màn hình ApplyActivity
        btnApplyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jobId == -1) {
                    Toast.makeText(JobDetailActivity.this, "Không tìm thấy ID công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(JobDetailActivity.this, ApplyActivity.class);
                // Truyền ID công việc và User ID sang để màn hình ApplyActivity làm thủ tục nộp đơn
                intent.putExtra("JOB_ID", jobId);
                intent.putExtra("USER_ID", currentUserId);
                intent.putExtra("JOB_TITLE_KEY", tvJobName.getText().toString());
                startActivity(intent);
            }
        });
    }
}