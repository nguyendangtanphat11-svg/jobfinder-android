package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.adapters.FavoriteAdapter;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Job;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity implements FavoriteAdapter.OnListEmptyListener {

    private RecyclerView rvFavoriteJobs;
    private FavoriteAdapter adapter;
    private List<Job> favoriteJobs = new ArrayList<>();
    private DBHelper dbHelper;
    private int currentUserId;
    private LinearLayout layoutEmpty;
    private SearchBar searchBar;
    private SearchView searchView;
    private RecyclerView rvSearchFavorite;
    private FavoriteAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        initViews();
        setupToolbar();
        setupUser();
        setupRecyclerView();
        setupSearch();
        loadData();
    }

    private void initViews() {
        rvFavoriteJobs = findViewById(R.id.rvFavoriteJobs);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        searchBar = findViewById(R.id.searchBar);
        searchView = findViewById(R.id.searchView);
        rvSearchFavorite = findViewById(R.id.rvSearchFavorite);
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

        if (currentUserId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem danh sách yêu thích", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void setupRecyclerView() {
        adapter = new FavoriteAdapter(this, favoriteJobs, currentUserId);
        rvFavoriteJobs.setLayoutManager(new LinearLayoutManager(this));
        rvFavoriteJobs.setAdapter(adapter);
    }

    private void setupSearch() {
        rvSearchFavorite.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new FavoriteAdapter(this, new ArrayList<>(), currentUserId);
        rvSearchFavorite.setAdapter(searchAdapter);

        searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            String query = searchView.getText().toString().trim();
            performSearch(query);
            return false;
        });

        searchView.addTransitionListener((searchView, previousState, newState) -> {
            if (newState == SearchView.TransitionState.HIDDEN) {
                loadData(); // Reload main list when closing search
            }
        });
    }

    private void performSearch(String keyword) {
        List<Job> results = dbHelper.searchFavoriteJobs(currentUserId, keyword);
        searchAdapter.updateList(results);
    }

    private void loadData() {
        favoriteJobs.clear();
        favoriteJobs.addAll(dbHelper.getFavoriteJobs(currentUserId));
        adapter.notifyDataSetChanged();
        checkEmptyState();
    }

    private void checkEmptyState() {
        if (favoriteJobs.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvFavoriteJobs.setVisibility(View.GONE);
            searchBar.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvFavoriteJobs.setVisibility(View.VISIBLE);
            searchBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onListEmpty() {
        checkEmptyState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
