package com.example.jobfinderapp.activities;

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
import com.example.jobfinderapp.adapters.ManageApplicantsAdapter;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Company;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManageApplicantsActivity extends AppCompatActivity {

    private RecyclerView rvApplicants;
    private ManageApplicantsAdapter adapter;
    private List<Map<String, Object>> applicantList = new ArrayList<>();
    private DBHelper dbHelper;
    private int companyId;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_applicants);

        dbHelper = new DBHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        int userId = pref.getInt("USER_ID", -1);
        Company company = dbHelper.getCompanyByUserId(userId);
        if (company != null) companyId = company.getId();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadApplicants();
    }

    private void initViews() {
        rvApplicants = findViewById(R.id.rvApplicants);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvApplicants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageApplicantsAdapter(this, applicantList, new ManageApplicantsAdapter.OnApplicantActionListener() {
            @Override
            public void onViewCV(int appId) {
                Toast.makeText(ManageApplicantsActivity.this, "Tính năng xem CV đang phát triển", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAccept(int appId) {
                updateStatus(appId, "Chấp nhận");
            }

            @Override
            public void onReject(int appId) {
                updateStatus(appId, "Từ chối");
            }
        });
        rvApplicants.setAdapter(adapter);
    }

    private void updateStatus(int appId, String status) {
        if (dbHelper.updateApplicationStatus(appId, status)) {
            Toast.makeText(this, "Đã cập nhật trạng thái: " + status, Toast.LENGTH_SHORT).show();
            loadApplicants();
        }
    }

    private void loadApplicants() {
        applicantList.clear();
        applicantList.addAll(dbHelper.getApplicantsByCompanyId(companyId));
        adapter.notifyDataSetChanged();

        if (applicantList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvApplicants.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvApplicants.setVisibility(View.VISIBLE);
        }
    }
}
