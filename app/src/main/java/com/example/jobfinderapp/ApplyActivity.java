package com.example.jobfinderapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
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

public class ApplyActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvApplyJobName, tvCVStatus;
    private EditText edtFullName, edtPhoneNumber, edtEmail;
    private LinearLayout layoutUploadCV;
    private Button btnSubmitApply;

    // Biến lưu trữ Uri của file thật sau khi chọn xong (dùng để gửi lên database/server sau này)
    private Uri selectedFileUri = null;

    // Bộ lắng nghe kết quả chọn file từ hệ thống điện thoại
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Lấy đường dẫn (Uri) của file thật người dùng vừa chọn
                    selectedFileUri = result.getData().getData();

                    if (selectedFileUri != null) {
                        // Lấy tên thật của file để hiển thị lên màn hình
                        String fileName = getFileNameFromUri(selectedFileUri);
                        tvCVStatus.setText("✅ Đã chọn: " + fileName);
                        Toast.makeText(this, "Đã đính kèm file thành công!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply);

        // 1. Ánh xạ các thành phần giao diện
        initViews();

        // 2. Nhận tên công việc từ màn hình trước chuyển qua
        String jobName = getIntent().getStringExtra("JOB_NAME_KEY");
        if (!TextUtils.isEmpty(jobName)) {
            tvApplyJobName.setText(jobName);
        }

        // 3. Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // 4. KẾT NỐI THẬT: Bấm vào vùng chọn CV sẽ mở bộ chọn tệp của điện thoại
        layoutUploadCV.setOnClickListener(v -> openFilePicker());

        // 5. Nút gửi hồ sơ
        btnSubmitApply.setOnClickListener(v -> validateAndSubmit());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvApplyJobName = findViewById(R.id.tvApplyJobName);
        tvCVStatus = findViewById(R.id.tvCVStatus);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtEmail = findViewById(R.id.edtEmail);
        layoutUploadCV = findViewById(R.id.layoutUploadCV);
        btnSubmitApply = findViewById(R.id.btnSubmitApply);
    }

    /**
     * Hàm kích hoạt Trình quản lý tệp tin của hệ điều hành Android
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Cho phép chọn mọi loại file

        // Bạn có thể giới hạn chỉ chọn file tài liệu bằng mảng MIME types dưới đây:
        String[] mimetypes = {"application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Mở màn hình chọn file
        filePickerLauncher.launch(Intent.createChooser(intent, "Chọn file CV ứng tuyển"));
    }

    /**
     * Hàm phụ trợ lấy tên file thật từ Uri hệ thống
     */
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void validateAndSubmit() {
        String name = edtFullName.getText().toString().trim();
        String phone = edtPhoneNumber.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            edtFullName.setError("Vui lòng nhập họ tên");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            edtPhoneNumber.setError("Vui lòng nhập số điện thoại");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập địa chỉ email");
            return;
        }

        // Kiểm tra xem biến Uri có dữ liệu thật hay chưa
        if (selectedFileUri == null) {
            Toast.makeText(this, "Vui lòng chọn file CV thực tế từ máy của bạn!", Toast.LENGTH_LONG).show();
            return;
        }

        // Thực tế: Biến `selectedFileUri` lúc này đang giữ file thật.
        // Khi Leader Tấn Phát làm xong tầng Database/API, bạn chỉ cần truyền biến `selectedFileUri.toString()` vào là xong!

        Toast.makeText(this, "🎉 Nộp hồ sơ thành công với file CV thật!", Toast.LENGTH_LONG).show();
        finish();
    }
}