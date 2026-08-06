package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.adapters.ApplicationAdapter;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.User;
import com.example.jobfinderapp.utils.UserSession;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ApplicationHistoryActivity extends AppCompatActivity {

    private RecyclerView rvApplications;
    private View layoutEmpty, layoutLoading;
    private TextView tvEmptyTitle, tvEmptyDescription;
    private TextInputEditText etSearchApplications;
    private ImageView btnClearSearch;
    private DBHelper dbHelper;
    private UserSession session;
    private ApplicationAdapter adapter;
    private User currentUser;
    private String selectedStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DBHelper.getInstance(this);
        session = new UserSession(this);
        if (!restoreCandidateSession()) return;

        setContentView(R.layout.activity_application_history);
        initViews();
        setupSearch();
        setupFilters();
        setupBottomNavigation();
        loadApplicationHistory();
    }

    private boolean restoreCandidateSession() {
        currentUser = session.isLoggedIn() ? dbHelper.getUserById(session.getUserId()) : null;
        if (currentUser != null && DBHelper.ROLE_CANDIDATE.equals(currentUser.getRole())
                && DBHelper.STATUS_ACTIVE.equals(currentUser.getStatus())) return true;
        session.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
        return false;
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        rvApplications = findViewById(R.id.rvApplications);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutLoading = findViewById(R.id.layoutLoading);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyDescription = findViewById(R.id.tvEmptyDescription);
        etSearchApplications = findViewById(R.id.etSearchApplications);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        rvApplications.setLayoutManager(new LinearLayoutManager(this));
        rvApplications.setHasFixedSize(true);
        adapter = new ApplicationAdapter(this, new ArrayList<>());
        rvApplications.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchApplications.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                loadApplicationHistory();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        btnClearSearch.setOnClickListener(v -> etSearchApplications.setText(""));
    }

    private void setupFilters() {
        ChipGroup filterGroup = findViewById(R.id.chipGroupStatus);
        filterGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != R.id.chipAll) {
                com.google.android.material.chip.Chip selected = group.findViewById(checkedId);
                selectedStatus = selected == null ? null : selected.getText().toString();
            } else selectedStatus = null;
            loadApplicationHistory();
        });
    }

    /* legacy implementation retained below only for source compatibility */
    private void setupFiltersLegacy() {
        ChipGroup filterGroup = findViewById(R.id.chipGroupStatus);
        filterGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipWaiting) selectedStatus = "Đang chờ";
            else if (checkedId == R.id.chipReviewing) selectedStatus = "Đang xem xét";
            else if (checkedId == R.id.chipAccepted) selectedStatus = "Đã chấp nhận";
            else if (checkedId == R.id.chipRejected) selectedStatus = "Đã từ chối";
            else selectedStatus = null;
            loadApplicationHistory();
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_application);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_application) return true;
            if (id == R.id.nav_home) startActivity(new Intent(this, HomeActivity.class));
            else if (id == R.id.nav_favorite) startActivity(new Intent(this, FavoriteActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
            return true;
        });
    }

    private void loadApplicationHistory() {
        if (currentUser == null || adapter == null) return;
        showState(true, false, null);
        try {
            String keyword = etSearchApplications == null ? "" : String.valueOf(etSearchApplications.getText());
            List<com.example.jobfinderapp.models.Job> history =
                    dbHelper.searchApplications(currentUser.getId(), keyword, selectedStatus);
            adapter.updateList(history);
            boolean empty = history.isEmpty();
            boolean hasFilter = !keyword.trim().isEmpty() || selectedStatus != null;
            showState(false, empty, hasFilter ? getString(R.string.application_empty_search_title)
                    : getString(R.string.application_empty_title), hasFilter
                    ? getString(R.string.application_empty_search_description)
                    : getString(R.string.application_empty_description));
        } catch (Exception exception) {
            adapter.updateList(new ArrayList<>());
            showState(false, true, getString(R.string.application_error_title),
                    getString(R.string.application_error_description));
        }
    }

    private void showState(boolean loading, boolean empty, String title) {
        showState(loading, empty, title, null);
    }

    private void showState(boolean loading, boolean empty, String title, String description) {
        layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        layoutEmpty.setVisibility(!loading && empty ? View.VISIBLE : View.GONE);
        rvApplications.setVisibility(!loading && !empty ? View.VISIBLE : View.GONE);
        if (title != null) tvEmptyTitle.setText(title);
        if (description != null) tvEmptyDescription.setText(description);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null && adapter != null) loadApplicationHistory();
    }
}
