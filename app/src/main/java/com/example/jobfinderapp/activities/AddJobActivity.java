package com.example.jobfinderapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Category;
import com.example.jobfinderapp.models.Company;
import com.example.jobfinderapp.models.Job;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AddJobActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etSalary, etLocation, etDescription, etRequirement, etDeadline;
    private AutoCompleteTextView spinnerCategory;
    private MaterialButton btnSave;
    private DBHelper dbHelper;
    private int jobId = -1;
    private int companyId;
    private List<Category> categories;
    private int selectedCategoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job);

        dbHelper = new DBHelper(this);
        jobId = getIntent().getIntExtra("JOB_ID", -1);

        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = pref.getInt("USER_ID", -1);
        Company company = dbHelper.getCompanyByUserId(userId);
        if (company != null) companyId = company.getId();

        initViews();
        setupToolbar();
        setupCategorySpinner();
        
        if (jobId != -1) {
            loadJobData();
            btnSave.setText("Cập nhật tin");
        }

        btnSave.setOnClickListener(v -> saveJob());
    }

    private void initViews() {
        etTitle = findViewById(R.id.etJobTitle);
        etSalary = findViewById(R.id.etSalary);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        etRequirement = findViewById(R.id.etRequirement);
        etDeadline = findViewById(R.id.etDeadline);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSaveJob);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (jobId != -1) {
            getSupportActionBar().setTitle("Chỉnh sửa tin tuyển dụng");
        }
    }

    private void setupCategorySpinner() {
        categories = dbHelper.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        for (Category cat : categories) {
            categoryNames.add(cat.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategoryId = categories.get(position).getId();
        });
    }

    private void loadJobData() {
        Job job = dbHelper.getJobById(jobId);
        if (job != null) {
            etTitle.setText(job.getTitle());
            etSalary.setText(job.getSalary());
            etLocation.setText(job.getLocation());
            etDescription.setText(job.getDescription());
            etRequirement.setText(job.getRequirement());
            etDeadline.setText(job.getDeadline());
            
            selectedCategoryId = job.getCategoryId();
            for (Category cat : categories) {
                if (cat.getId() == selectedCategoryId) {
                    spinnerCategory.setText(cat.getName(), false);
                    break;
                }
            }
        }
    }

    private void saveJob() {
        String title = etTitle.getText().toString().trim();
        String salary = etSalary.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String req = etRequirement.getText().toString().trim();
        String deadline = etDeadline.getText().toString().trim();

        if (title.isEmpty() || selectedCategoryId == -1 || salary.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        Job job = new Job();
        job.setTitle(title);
        job.setCategoryId(selectedCategoryId);
        job.setCompanyId(companyId);
        job.setSalary(salary);
        job.setLocation(location);
        job.setDescription(desc);
        job.setRequirement(req);
        job.setDeadline(deadline);

        boolean success;
        if (jobId == -1) {
            success = dbHelper.addJob(job);
        } else {
            job.setId(jobId);
            job.setStatus(dbHelper.getJobById(jobId).getStatus()); // Giữ nguyên trạng thái cũ
            success = dbHelper.updateJob(job);
        }

        if (success) {
            Toast.makeText(this, jobId == -1 ? "Đăng tin thành công" : "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}
