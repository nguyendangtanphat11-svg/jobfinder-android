package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.adapters.CategoryAdapter;
import com.example.jobfinderapp.adapters.JobAdapter;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Category;
import com.example.jobfinderapp.models.User;
import com.example.jobfinderapp.utils.UserSession;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private UserSession session;
    private User currentUser;
    private int selectedCategoryId = 0;

    private TextView tvGreeting, tvUserName, tvNotificationBadge, tvEmpty, tvJobCount;
    private ImageView ivAvatar, ivNotification, btnClearSearch;
    private TextInputEditText etSearchKeyword;
    private View layoutLoading, layoutEmpty, contentJobs;
    private RecyclerView rvCategories, rvJobs;
    private BottomNavigationView bottomNavigation;
    private JobAdapter jobAdapter;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DBHelper.getInstance(this);
        session = new UserSession(this);
        if (!restoreCandidateSession()) return;

        setContentView(R.layout.activity_home);
        dbHelper.insertSampleJobs();
        initViews();
        bindUserHeader();
        setupLists();
        setupSearch();
        setupNotification();
        setupBottomNavigation();
        loadCategories();
        loadJobs();
    }

    @Override protected void onResume() {
        super.onResume();
        if (dbHelper == null || !restoreCandidateSession()) return;
        // Reload header too: the profile screen can change the persisted avatar.
        if (ivAvatar != null) bindUserHeader();
        if (jobAdapter != null) loadJobs();
        updateUnreadNotifications();
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
        tvGreeting = findViewById(R.id.tvGreeting); tvUserName = findViewById(R.id.tvUserName);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge); tvEmpty = findViewById(R.id.tvEmpty);
        tvJobCount = findViewById(R.id.tvJobCount); ivAvatar = findViewById(R.id.ivAvatar);
        ivNotification = findViewById(R.id.ivNotification); btnClearSearch = findViewById(R.id.btnClearSearch);
        etSearchKeyword = findViewById(R.id.etSearchKeyword); layoutLoading = findViewById(R.id.layoutLoading);
        layoutEmpty = findViewById(R.id.layoutEmpty); contentJobs = findViewById(R.id.contentJobs);
        rvCategories = findViewById(R.id.rvCategories); rvJobs = findViewById(R.id.rvJobs);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void bindUserHeader() {
        String name = currentUser.getFullname() == null || currentUser.getFullname().trim().isEmpty() ? "bạn" : currentUser.getFullname().trim();
        tvGreeting.setText("Chào buổi tốt lành,");
        tvUserName.setText(name);
Glide.with(this).load(currentUser.getAvatar()).placeholder(R.drawable.ic_default_avatar).error(R.drawable.ic_default_avatar).into(ivAvatar);
    }

    private void setupLists() {
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        jobAdapter = new JobAdapter(this, currentUser.getId(), (job, isFavorite) -> {
            boolean changed = dbHelper.toggleFavorite(currentUser.getId(), job.getId());
            if (changed) {
                jobAdapter.notifyDataSetChanged();
                Toast.makeText(this, isFavorite ? "Đã bỏ lưu việc làm" : "Đã lưu việc làm", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể cập nhật việc làm đã lưu", Toast.LENGTH_SHORT).show();
            }
        });
        rvJobs.setAdapter(jobAdapter);
    }

    private void loadCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(0, "Tất cả"));
        categories.addAll(dbHelper.getAllCategories());
        categoryAdapter = new CategoryAdapter(categories, category -> {
            selectedCategoryId = category.getId();
            loadJobs();
        });
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupSearch() {
        etSearchKeyword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
                loadJobs();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        btnClearSearch.setOnClickListener(v -> etSearchKeyword.setText(""));
    }

    private void loadJobs() {
        if (jobAdapter == null) return;
        showState(true, false, null);
        try {
            String keyword = etSearchKeyword == null ? "" : etSearchKeyword.getText().toString();
            List<com.example.jobfinderapp.models.Job> jobs = dbHelper.getJobsForHome(keyword, selectedCategoryId);
            jobAdapter.updateList(jobs);
            tvJobCount.setText(jobs.isEmpty() ? "" : jobs.size() + " việc làm phù hợp");
            showState(false, jobs.isEmpty(), jobs.isEmpty() ? "Không tìm thấy việc làm phù hợp" : null);
        } catch (Exception exception) {
            jobAdapter.updateList(new ArrayList<>());
            showState(false, true, "Không thể tải danh sách việc làm. Vui lòng thử lại.");
        }
    }

    private void showState(boolean loading, boolean empty, String message) {
        layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        layoutEmpty.setVisibility(!loading && empty ? View.VISIBLE : View.GONE);
        contentJobs.setVisibility(!loading && !empty ? View.VISIBLE : View.GONE);
        if (message != null) tvEmpty.setText(message);
    }

    private void setupNotification() {
        ivNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        updateUnreadNotifications();
    }

    private void updateUnreadNotifications() {
        if (currentUser == null || tvNotificationBadge == null) return;
        int count = dbHelper.getUnreadNotificationCount(currentUser.getId());
        tvNotificationBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        tvNotificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_favorite) startActivity(new Intent(this, FavoriteActivity.class));
            else if (id == R.id.nav_application) startActivity(new Intent(this, ApplicationHistoryActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
            return true;
        });
    }
}
