package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.User;

public class ApplyActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnSubmitApply;
    private EditText edtFullName, edtPhoneNumber, edtEmail;
    private TextView tvApplyJobName, tvCVStatus;
    private LinearLayout layoutUploadCV;

    private DBHelper dbHelper;
    private int jobId = -1;
    private int currentUserId = -1;
    private boolean isCVSelected = false; // Biến cờ giả lập kiểm tra việc chọn CV

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        dbHelper = new DBHelper(this);

        // 1. Ánh xạ View từ file XML của Kiệt
        initViews();

        // 2. Đồng bộ User Session giống như trang Yêu thích của Giàu
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = pref.getInt("USER_ID", -1);
        if (currentUserId == -1) {
            currentUserId = 2; // Ép thử bằng Candidate mẫu "Nguyễn Văn A" nếu chạy test độc lập
        }

        // 3. Nhận dữ liệu ID và Tiêu đề công việc chuyển tiếp từ màn hình JobDetailActivity sang
        jobId = getIntent().getIntExtra("JOB_ID", -1);
        String jobTitle = getIntent().getStringExtra("JOB_TITLE_KEY");

        if (jobTitle != null) {
            tvApplyJobName.setText(jobTitle);
        }

        // 4. Tự động điền trước thông tin cá nhân của User từ Database cho tiện
        autoFillUserData();

        // 5. Cài đặt các sự kiện nút bấm
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSubmitApply = findViewById(R.id.btnSubmitApply);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtEmail = findViewById(R.id.edtEmail);
        tvApplyJobName = findViewById(R.id.tvApplyJobName);
        tvCVStatus = findViewById(R.id.tvCVStatus);
        layoutUploadCV = findViewById(R.id.layoutUploadCV);
    }

    private void autoFillUserData() {
        User user = dbHelper.getUserById(currentUserId);
        if (user != null) {
            edtFullName.setText(user.getFullname());
            edtEmail.setText(user.getEmail());
            if (user.getPhone() != null) {
                edtPhoneNumber.setText(user.getPhone());
            }
        }
    }

    private void setupClickListeners() {
        // Sự kiện nhấn nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Sự kiện click vào khung chọn CV
        layoutUploadCV.setOnClickListener(v -> {
            // Giả lập hành động chọn file (Ở mức đồ án môn học, cập nhật UI để kiểm tra trước)
            isCVSelected = true;
            tvCVStatus.setText("✅ Đã chọn: My_CV_Profile.pdf");
            tvCVStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            Toast.makeText(this, "Chọn tệp CV thành công!", Toast.LENGTH_SHORT).show();
        });

        // XỬ LÝ SỰ KIỆN GỬI HỒ SƠ ỨNG TUYỂN
        // XỬ LÝ SỰ KIỆN GỬI HỒ SƠ ỨNG TUYỂN
        btnSubmitApply.setOnClickListener(v -> {
            String name = edtFullName.getText().toString().trim();
            String phone = edtPhoneNumber.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();

            // 1. Ràng buộc kiểm tra nhập liệu cơ bản
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ các trường thông tin bắt buộc (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isCVSelected) {
                Toast.makeText(this, "Vui lòng đính kèm tệp CV trước khi gửi!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (jobId == -1) {
                Toast.makeText(this, "Lỗi: Không xác định được công việc ứng tuyển!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Gọi hàm chèn bản ghi ứng tuyển vào bảng applications trong SQLite
            boolean isApplied = dbHelper.applyJob(currentUserId, jobId);

            if (isApplied) {
                Toast.makeText(this, "🎉 Nộp hồ sơ ứng tuyển thành công!", Toast.LENGTH_LONG).show();

                // 3. Tự động thêm một thông báo hệ thống (Tùy chọn)
                dbHelper.addNotification(currentUserId, "Ứng tuyển thành công", "Bạn đã nộp đơn ứng tuyển cho vị trí " + tvApplyJobName.getText().toString());

                // 5. Đóng màn hình nộp đơn hiện tại để khi từ trang Lịch sử bấm Back sẽ không bị quay lại trang điền form này nữa
                finish();
            } else {
                Toast.makeText(this, "Thao tác gửi đơn thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}