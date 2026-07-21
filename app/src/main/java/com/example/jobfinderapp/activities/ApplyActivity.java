package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private boolean isCVSelected = false;

    // Bộ lắng nghe kết quả chọn file từ hệ thống
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        dbHelper = new DBHelper(this);

        // Đăng ký Callback xử lý kết quả chọn file thật trả về từ hệ thống
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            // Trích xuất tên file từ URI để hiển thị lên UI
                            String filePath = fileUri.getPath();
                            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

                            // Cập nhật trạng thái giao diện trực quan
                            isCVSelected = true;
                            tvCVStatus.setText("✅ Đã chọn: " + fileName);
                            tvCVStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                            Toast.makeText(this, "Tải tệp CV lên thành công!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // 1. Ánh xạ View từ file XML
        initViews();

        // 2. Đồng bộ User Session giống như trang Yêu thích
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

        // 5. Cài đặt các sự kiện nút bấm và kiểm tra quyền tương tác
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

        // Sự kiện xử lý chọn tệp CV (Sử dụng Intent.ACTION_GET_CONTENT không cần xin quyền runtime)
        layoutUploadCV.setOnClickListener(v -> openFilePicker());

        // XỬ LÝ SỰ KIỆN GỬI HỒ SƠ ỨNG TUYỂN VÀO DATABASE SQLite
        btnSubmitApply.setOnClickListener(v -> {
            String name = edtFullName.getText().toString().trim();
            String phone = edtPhoneNumber.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();

            // Ràng buộc kiểm tra nhập liệu cơ bản (Data Validation)
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

            // Gọi hàm chèn bản ghi ứng tuyển vào bảng applications trong SQLite
            boolean isApplied = dbHelper.applyJob(currentUserId, jobId);

            if (isApplied) {
                Toast.makeText(this, "🎉 Nộp hồ sơ ứng tuyển thành công!", Toast.LENGTH_LONG).show();

                // Tự động thêm một thông báo hệ thống đồng bộ
                dbHelper.addNotification(currentUserId, "Ứng tuyển thành công", "Bạn đã nộp đơn ứng tuyển cho vị trí " + tvApplyJobName.getText().toString());

                // Giải phóng màn hình hiện tại ra khỏi Activity Stack
                finish();
            } else {
                Toast.makeText(this, "Thao tác gửi đơn thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm độc lập đóng gói Intent mở Storage Access Framework để chọn file tài liệu
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Cho phép quét tất cả các file ban đầu

        // Thiết lập bộ lọc định dạng tài liệu văn bản chuẩn (PDF, DOC, DOCX)
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        filePickerLauncher.launch(intent);
    }
}