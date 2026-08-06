package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Job;
import com.example.jobfinderapp.models.User;
import com.example.jobfinderapp.utils.UserSession;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JobDetailActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private UserSession session;
    private User currentUser;
    private Job job;
    private int jobId = -1;
    private ActivityResultLauncher<Intent> applyLauncher;

    private ImageView btnClose, btnFavorite, ivCompanyLogo;
    private MaterialButton btnApplyNow;
    private TextView tvCompanyName, tvJobName, tvJobSalary, tvJobLocation, tvJobExperience,
            tvJobDescription, tvJobAddress, tvCompanyAddress, tvDeadline, tvEducation,
            tvQuantity, tvAge, tvJobType, tvGender, tvJobStatus, tvRequirement, tvError;
    private View layoutLoading, layoutContent, layoutError;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DBHelper.getInstance(this);
        session = new UserSession(this);
        if (!restoreCandidateSession()) return;
        setContentView(R.layout.activity_job_detail);
        initViews();
        applyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && job != null) updateApplyState();
        });
        jobId = getIntent().getIntExtra("JOB_ID", -1);
        if (jobId <= 0) {
            showMissingJobAndFinish();
            return;
        }
        setupActions();
        loadJob();
    }

    @Override protected void onResume() {
        super.onResume();
        if (job != null && currentUser != null) updateApplyState();
    }

    private boolean restoreCandidateSession() {
        currentUser = session.isLoggedIn() ? dbHelper.getUserById(session.getUserId()) : null;
        if (currentUser == null || !DBHelper.ROLE_CANDIDATE.equals(currentUser.getRole())
                || !DBHelper.STATUS_ACTIVE.equals(currentUser.getStatus())) {
            session.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return false;
        }
        session.save(currentUser);
        return true;
    }

    private void initViews() {
        btnClose = findViewById(R.id.btnClose); btnFavorite = findViewById(R.id.btnFavorite);
        ivCompanyLogo = findViewById(R.id.ivCompanyLogo); btnApplyNow = findViewById(R.id.btnApplyNow);
        tvCompanyName = findViewById(R.id.tvCompanyName); tvJobName = findViewById(R.id.tvJobName);
        tvJobSalary = findViewById(R.id.tvJobSalary); tvJobLocation = findViewById(R.id.tvJobLocation);
        tvJobExperience = findViewById(R.id.tvJobExperience); tvJobDescription = findViewById(R.id.tvJobDescription);
        tvRequirement = findViewById(R.id.tvRequirement);
        tvJobAddress = findViewById(R.id.tvJobAddress); tvCompanyAddress = findViewById(R.id.tvCompanyAddress);
        tvDeadline = findViewById(R.id.tvDeadline); tvEducation = findViewById(R.id.tvEducation);
        tvQuantity = findViewById(R.id.tvQuantity); tvAge = findViewById(R.id.tvAge);
        tvJobType = findViewById(R.id.tvJobType); tvGender = findViewById(R.id.tvGender);
        tvJobStatus = findViewById(R.id.tvJobStatus); tvError = findViewById(R.id.tvError);
        layoutLoading = findViewById(R.id.layoutLoading); layoutContent = findViewById(R.id.layoutContent);
        layoutError = findViewById(R.id.layoutError);
    }

    private void setupActions() {
        btnClose.setOnClickListener(v -> finish());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnApplyNow.setOnClickListener(v -> openApply());
    }

    private void loadJob() {
        showState(true, false);
        job = dbHelper.getJobById(jobId);
        if (job == null) {
            showMissingJobAndFinish();
            return;
        }
        bindJob();
        updateFavoriteState();
        updateApplyState();
        showState(false, false);
    }

    private void bindJob() {
        tvJobName.setText(text(job.getTitle(), "Công việc đang cập nhật"));
        tvCompanyName.setText(text(job.getCompanyName(), "Công ty đang cập nhật"));
        tvJobSalary.setText(text(job.getSalary(), "Thỏa thuận"));
        tvJobLocation.setText(text(job.getLocation(), "Linh hoạt"));
        tvJobType.setText(text(job.getJobType(), "Đang cập nhật"));
        tvJobExperience.setText(text(job.getExperience(), "Đang cập nhật"));
        tvEducation.setText(text(job.getEducation(), "Đang cập nhật"));
        tvQuantity.setText(job.getQuantity() > 0 ? String.valueOf(job.getQuantity()) : "Đang cập nhật");
        tvAge.setText(text(job.getAge(), "Không yêu cầu"));
        tvGender.setText(text(job.getGender(), "Không yêu cầu"));
        tvJobDescription.setText(text(job.getDescription(), "Chưa có mô tả công việc."));
        tvRequirement.setText(text(job.getRequirement(), "Chưa có yêu cầu cụ thể."));
        tvJobAddress.setText(text(job.getLocation(), "Đang cập nhật"));
        tvCompanyAddress.setText(text(job.getCompanyAddress(), text(job.getLocation(), "Đang cập nhật")));
        tvDeadline.setText(text(job.getDeadline(), "Chưa cập nhật"));
        tvJobStatus.setText(text(job.getStatus(), "Đang tuyển"));
Glide.with(this).load(job.getCompanyLogo()).placeholder(R.drawable.ic_default_company).error(R.drawable.ic_default_company).into(ivCompanyLogo);
    }

    private void updateFavoriteState() {
        boolean saved = dbHelper.isFavorite(currentUser.getId(), jobId);
        btnFavorite.setImageResource(saved ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        btnFavorite.setContentDescription(saved ? "Bỏ lưu việc làm" : "Lưu việc làm");
    }

    private void toggleFavorite() {
        if (dbHelper.toggleFavorite(currentUser.getId(), jobId)) {
            updateFavoriteState();
        } else {
            Toast.makeText(this, "Không thể cập nhật việc làm đã lưu", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateApplyState() {
        boolean alreadyApplied = dbHelper.hasAlreadyApplied(currentUser.getId(), jobId);
        boolean recruiting = "Đang tuyển".equals(job.getStatus());
        boolean expired = isExpired(job.getDeadline());
        if (alreadyApplied) {
            btnApplyNow.setText("Đã ứng tuyển"); btnApplyNow.setEnabled(false);
        } else if (!recruiting) {
            btnApplyNow.setText("Đã đóng tuyển"); btnApplyNow.setEnabled(false);
        } else if (expired) {
            btnApplyNow.setText("Đã hết hạn nộp"); btnApplyNow.setEnabled(false);
        } else {
            btnApplyNow.setText("Ứng tuyển ngay"); btnApplyNow.setEnabled(true);
        }
    }

    private void openApply() {
        if (dbHelper.hasAlreadyApplied(currentUser.getId(), jobId)) {
            updateApplyState();
            return;
        }
        Intent intent = new Intent(this, ApplyActivity.class);
        intent.putExtra("JOB_ID", jobId);
        intent.putExtra("JOB_TITLE_KEY", job.getTitle());
        applyLauncher.launch(intent);
    }

    private boolean isExpired(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) return false;
        String[] formats = {"dd/MM/yyyy", "yyyy-MM-dd"};
        for (String format : formats) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(format, Locale.ROOT);
                parser.setLenient(false);
                Date date = parser.parse(deadline.trim());
                Calendar endOfDay = Calendar.getInstance();
                endOfDay.setTime(date);
                endOfDay.set(Calendar.HOUR_OF_DAY, 23); endOfDay.set(Calendar.MINUTE, 59); endOfDay.set(Calendar.SECOND, 59);
                return endOfDay.getTime().before(new Date());
            } catch (ParseException ignored) { }
        }
        return false;
    }

    private void showState(boolean loading, boolean error) {
        layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        layoutError.setVisibility(error ? View.VISIBLE : View.GONE);
        layoutContent.setVisibility(!loading && !error ? View.VISIBLE : View.GONE);
    }

    private void showMissingJobAndFinish() {
        if (tvError == null) {
            Toast.makeText(this, "Không tìm thấy thông tin công việc", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvError.setText("Công việc không tồn tại hoặc đã bị xóa.");
        showState(false, true);
        btnApplyNow.setEnabled(false);
        btnFavorite.setEnabled(false);
        new Handler().postDelayed(this::finish, 1200);
    }

    private String text(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value; }
}
