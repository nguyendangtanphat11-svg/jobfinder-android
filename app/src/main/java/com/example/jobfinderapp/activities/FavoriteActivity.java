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
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.adapters.FavoriteAdapter;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Job;
import com.example.jobfinderapp.models.User;
import com.example.jobfinderapp.utils.UserSession;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView rvFavoriteJobs;
    private FavoriteAdapter adapter;
    private DBHelper dbHelper;
    private UserSession session;
    private User currentUser;
    private View layoutEmpty, layoutLoading;
    private TextView tvEmptyTitle, tvEmptyDescription;
    private TextInputEditText etSearchFavorite;
    private ImageView btnClearSearch;
    private String selectedFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DBHelper.getInstance(this);
        session = new UserSession(this);
        if (!restoreCandidateSession()) return;

        setContentView(R.layout.activity_favorite);
        initViews();
        setupSearch();
        setupFilters();
        setupBottomNavigation();
        loadFavoriteJobs();
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
        rvFavoriteJobs = findViewById(R.id.rvFavoriteJobs);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutLoading = findViewById(R.id.layoutLoading);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyDescription = findViewById(R.id.tvEmptyDescription);
        etSearchFavorite = findViewById(R.id.etSearchFavorite);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        rvFavoriteJobs.setLayoutManager(new LinearLayoutManager(this));
        rvFavoriteJobs.setHasFixedSize(true);
        adapter = new FavoriteAdapter(this, new ArrayList<>(), this::removeFavorite);
        rvFavoriteJobs.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchFavorite.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                loadFavoriteJobs();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        btnClearSearch.setOnClickListener(v -> etSearchFavorite.setText(""));
    }

    private void setupFilters() {
        ChipGroup chipGroup = findViewById(R.id.chipGroupFavoriteStatus);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipRecruiting) selectedFilter = "recruiting";
            else if (checkedId == R.id.chipClosed) selectedFilter = "closed";
            else selectedFilter = null;
            loadFavoriteJobs();
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_favorite);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_favorite) return true;
            if (id == R.id.nav_home) startActivity(new Intent(this, HomeActivity.class));
            else if (id == R.id.nav_application) startActivity(new Intent(this, ApplicationHistoryActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
            return true;
        });
    }

    private void loadFavoriteJobs() {
        if (currentUser == null || adapter == null) return;
        showState(true, false, null, null);
        try {
            String keyword = etSearchFavorite == null ? "" : String.valueOf(etSearchFavorite.getText());
            List<Job> jobs = dbHelper.getFavoriteJobsForCandidate(currentUser.getId(), keyword, selectedFilter);
            adapter.updateList(jobs);
            boolean hasFilter = !keyword.trim().isEmpty() || selectedFilter != null;
            showState(false, jobs.isEmpty(), hasFilter ? getString(R.string.favorite_empty_search_title)
                    : getString(R.string.favorite_empty_title), hasFilter
                    ? getString(R.string.favorite_empty_search_description)
                    : getString(R.string.favorite_empty_description));
        } catch (Exception exception) {
            adapter.updateList(new ArrayList<>());
            showState(false, true, getString(R.string.favorite_error_title),
                    getString(R.string.favorite_error_description));
        }
    }

    private void removeFavorite(Job job) {
        if (currentUser == null) return;
        if (dbHelper.removeFavorite(currentUser.getId(), job.getId())) {
            adapter.removeByJobId(job.getId());
            Toast.makeText(this, R.string.favorite_removed, Toast.LENGTH_SHORT).show();
            if (adapter.getItemCount() == 0) loadFavoriteJobs();
        } else {
            Toast.makeText(this, R.string.favorite_remove_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void showState(boolean loading, boolean empty, String title, String description) {
        layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        layoutEmpty.setVisibility(!loading && empty ? View.VISIBLE : View.GONE);
        rvFavoriteJobs.setVisibility(!loading && !empty ? View.VISIBLE : View.GONE);
        if (title != null) tvEmptyTitle.setText(title);
        if (description != null) tvEmptyDescription.setText(description);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null && adapter != null) loadFavoriteJobs();
    }
}
