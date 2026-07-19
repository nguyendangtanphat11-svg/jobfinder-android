package com.example.jobfinderapp.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.adapters.ApplicationAdapter;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Job;

import java.util.List;

public class ApplicationHistoryActivity extends AppCompatActivity {

    private RecyclerView rvApplications;
    private LinearLayout layoutEmpty;
    private DBHelper dbHelper;
    private ApplicationAdapter adapter;
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_history);

        dbHelper = new DBHelper(this);
        
        // Get user ID (in a real app, this would come from SharedPreferences or Session)
        // Here we try to get it from Intent or fallback to the first user found for demo
        setupUser();
        initViews();
        loadApplicationHistory();
    }

    private void setupUser() {
        // Lấy ID người dùng từ SharedPreferences giống hệt bên trang ApplyActivity
        android.content.SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = pref.getInt("USER_ID", -1);

        if (currentUserId == -1) {
            currentUserId = 2; // Dự phòng lỗi nếu chưa đăng nhập
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lịch sử ứng tuyển");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvApplications = findViewById(R.id.rvApplications);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        
        rvApplications.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadApplicationHistory() {
        if (currentUserId == -1) return;

        List<Job> history = dbHelper.getApplicationHistory(currentUserId);
        
        if (history.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvApplications.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvApplications.setVisibility(View.VISIBLE);
            adapter = new ApplicationAdapter(this, history);
            rvApplications.setAdapter(adapter);
        }
    }
}
