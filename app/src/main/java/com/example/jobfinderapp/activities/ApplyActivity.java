package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.CV;
import com.example.jobfinderapp.models.Job;
import com.example.jobfinderapp.models.User;
import com.example.jobfinderapp.utils.UserSession;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ApplyActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private UserSession session;
    private User currentUser;
    private Job job;
    private int jobId = -1;
    private boolean submitting;
    private String selectedCvReference;

    private Toolbar toolbar;
    private MaterialButton btnSubmitApply;
    private TextInputEditText edtFullName, edtPhoneNumber, edtEmail, edtMessage;
    private TextView tvApplyJobName, tvApplyCompanyName, tvCVStatus;
    private LinearLayout layoutUploadCV;
    private MaterialCheckBox cbConfirmInfo;
    private View layoutLoading;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DBHelper.getInstance(this);
        session = new UserSession(this);
        if (!restoreCandidateSession()) return;
        setContentView(R.layout.activity_apply);
        initViews();
        registerFilePicker();
        jobId = getIntent().getIntExtra("JOB_ID", -1);
        if (jobId <= 0 || !loadAndValidateJob()) return;
        autoFillUserData();
        loadStoredCv();
        setupClickListeners();
    }

    private boolean restoreCandidateSession() {
        currentUser = session.isLoggedIn() ? dbHelper.getUserById(session.getUserId()) : null;
        if (currentUser == null || !DBHelper.ROLE_CANDIDATE.equals(currentUser.getRole())
                || !DBHelper.STATUS_ACTIVE.equals(currentUser.getStatus())) {
            session.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent); finish(); return false;
        }
        session.save(currentUser);
        return true;
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar); btnSubmitApply = findViewById(R.id.btnSubmitApply);
        edtFullName = findViewById(R.id.edtFullName); edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtEmail = findViewById(R.id.edtEmail); edtMessage = findViewById(R.id.edtMessage);
        tvApplyJobName = findViewById(R.id.tvApplyJobName); tvApplyCompanyName = findViewById(R.id.tvApplyCompanyName);
        tvCVStatus = findViewById(R.id.tvCVStatus); layoutUploadCV = findViewById(R.id.layoutUploadCV);
        cbConfirmInfo = findViewById(R.id.cbConfirmInfo); layoutLoading = findViewById(R.id.layoutLoading);
    }

    private boolean loadAndValidateJob() {
        job = dbHelper.getJobById(jobId);
        if (job == null) return closeWithMessage("Không tìm thấy thông tin công việc");
        if (!"Đang tuyển".equals(job.getStatus())) return closeWithMessage("Công việc này đã đóng tuyển");
        if (isExpired(job.getDeadline())) return closeWithMessage("Công việc đã hết hạn nộp hồ sơ");
        if (dbHelper.hasAlreadyApplied(currentUser.getId(), jobId)) return closeWithMessage("Bạn đã ứng tuyển công việc này");
        tvApplyJobName.setText(job.getTitle());
        tvApplyCompanyName.setText(job.getCompanyName());
        return true;
    }

    private boolean closeWithMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); finish(); return false;
    }

    private void autoFillUserData() {
        edtFullName.setText(currentUser.getFullname()); edtEmail.setText(currentUser.getEmail());
        if (currentUser.getPhone() != null) edtPhoneNumber.setText(currentUser.getPhone());
    }

    private void loadStoredCv() {
        CV cv = dbHelper.getCVByUserId(currentUser.getId());
        if (cv == null) {
            tvCVStatus.setText("Bạn chưa có CV đã lưu. Hãy chọn tệp CV để tiếp tục.");
            return;
        }
        selectedCvReference = "saved_cv:" + cv.getId();
        String name = cv.getCvName() == null || cv.getCvName().trim().isEmpty() ? "CV đã lưu" : cv.getCvName();
        tvCVStatus.setText("Đang dùng CV đã lưu: " + name + ". Nhấn để thay bằng tệp khác.");
    }

    private void registerFilePicker() {
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
            Uri uri = result.getData().getData();
            try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (SecurityException ignored) { }
            selectedCvReference = uri.toString();
            tvCVStatus.setText("Đã chọn tệp CV: " + getDisplayName(uri));
        });
    }

    private String getDisplayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return cursor.getString(index);
            }
        }
        return "Tệp CV đã chọn";
    }

    private void setupClickListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        layoutUploadCV.setOnClickListener(v -> openFilePicker());
        btnSubmitApply.setOnClickListener(v -> submitApplication());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE); intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"});
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        filePickerLauncher.launch(intent);
    }

    private void submitApplication() {
        if (submitting) return;
        if (dbHelper.hasAlreadyApplied(currentUser.getId(), jobId)) { closeWithMessage("Bạn đã ứng tuyển công việc này"); return; }
        String name = edtFullName.getText().toString().trim();
        String phone = edtPhoneNumber.getText().toString().trim();
        String email = DBHelper.normalizeEmail(edtEmail.getText().toString());
        String message = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(name)) { edtFullName.setError("Vui lòng nhập họ tên"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { edtEmail.setError("Email không hợp lệ"); return; }
        if (!phone.matches("^[0-9+][0-9 .-]{7,14}$")) { edtPhoneNumber.setError("Số điện thoại không hợp lệ"); return; }
        if (TextUtils.isEmpty(selectedCvReference)) { Toast.makeText(this, "Vui lòng chọn CV đã lưu hoặc tệp CV", Toast.LENGTH_SHORT).show(); return; }
        if (message.length() > 1000) { edtMessage.setError("Lời nhắn tối đa 1000 ký tự"); return; }
        if (!cbConfirmInfo.isChecked()) { cbConfirmInfo.setError("Vui lòng xác nhận thông tin"); return; }
        setLoading(true);
        boolean applied = dbHelper.applyJob(currentUser.getId(), jobId, name, phone, email, selectedCvReference, message);
        if (!applied) {
            setLoading(false);
            Toast.makeText(this, "Không thể gửi hồ sơ. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            return;
        }
        dbHelper.addNotification(currentUser.getId(), "Ứng tuyển thành công", "Hồ sơ cho '" + job.getTitle() + "' đã được gửi.");
        setResult(RESULT_OK);
        Toast.makeText(this, "Ứng tuyển thành công", Toast.LENGTH_LONG).show();
        finish();
    }

    private void setLoading(boolean loading) {
        submitting = loading; layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmitApply.setEnabled(!loading); layoutUploadCV.setEnabled(!loading);
    }

    private boolean isExpired(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) return false;
        for (String format : new String[]{"dd/MM/yyyy", "yyyy-MM-dd"}) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(format, Locale.ROOT); parser.setLenient(false);
                Date date = parser.parse(deadline.trim()); Calendar end = Calendar.getInstance(); end.setTime(date);
                end.set(Calendar.HOUR_OF_DAY, 23); end.set(Calendar.MINUTE, 59); end.set(Calendar.SECOND, 59);
                return end.getTime().before(new Date());
            } catch (ParseException ignored) { }
        }
        return false;
    }
}
