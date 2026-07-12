package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.adapters.ManageJobsAdapter;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Company;
import com.example.jobfinderapp.models.Job;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ManageJobsActivity extends AppCompatActivity {

    private RecyclerView rvManageJobs;
    private ManageJobsAdapter adapter;
    private List<Job> jobList = new ArrayList<>();
    private DBHelper dbHelper;
    private int companyId;
    private LinearLayout layoutEmpty;
    private ExtendedFloatingActionButton fabAddJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_jobs);

        dbHelper = new DBHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = pref.getInt("USER_ID", -1);
        Company company = dbHelper.getCompanyByUserId(userId);
        if (company != null) companyId = company.getId();

        initViews();
        setupToolbar();
        setupRecyclerView();
        
        fabAddJob.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddJobActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        rvManageJobs = findViewById(R.id.rvManageJobs);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        fabAddJob = findViewById(R.id.fabAddJob);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvManageJobs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageJobsAdapter(this, jobList, new ManageJobsAdapter.OnJobActionListener() {
            @Override
            public void onEdit(Job job) {
                Intent intent = new Intent(ManageJobsActivity.this, AddJobActivity.class);
                intent.putExtra("JOB_ID", job.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Job job) {
                new AlertDialog.Builder(ManageJobsActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa tin tuyển dụng này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            if (dbHelper.deleteJob(job.getId())) {
                                Toast.makeText(ManageJobsActivity.this, "Đã xóa tin", Toast.LENGTH_SHORT).show();
                                loadJobs();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        rvManageJobs.setAdapter(adapter);
    }

    private void loadJobs() {
        jobList.clear();
        jobList.addAll(dbHelper.getJobsByCompanyId(companyId));
        adapter.notifyDataSetChanged();

        if (jobList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvManageJobs.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvManageJobs.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJobs();
    }
}
