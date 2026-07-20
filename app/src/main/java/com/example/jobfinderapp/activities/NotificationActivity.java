package com.example.jobfinderapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.adapters.NotificationAdapter;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Notification;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private DBHelper dbHelper;
    private int currentUserId;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initViews();
        setupToolbar();
        setupUser();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        dbHelper = new DBHelper(this);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupUser() {
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = pref.getInt("USER_ID", -1);
    }

    private void setupRecyclerView() {
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, notificationList);
        rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        if (currentUserId != -1) {
            notificationList.clear();
            notificationList.addAll(dbHelper.getNotifications(currentUserId));
            adapter.notifyDataSetChanged();

            if (notificationList.isEmpty()) {
                layoutEmpty.setVisibility(View.VISIBLE);
                rvNotifications.setVisibility(View.GONE);
            } else {
                layoutEmpty.setVisibility(View.GONE);
                rvNotifications.setVisibility(View.VISIBLE);
            }
        } else {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}
