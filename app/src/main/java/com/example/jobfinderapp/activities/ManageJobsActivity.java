package com.example.jobfinderapp.activities;

import android.content.Intent;
import android.text.Editable;import android.text.TextWatcher;import android.widget.EditText;
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
import com.example.jobfinderapp.models.User;import com.example.jobfinderapp.utils.UserSession;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ManageJobsActivity extends AppCompatActivity {

    private RecyclerView rvManageJobs;
    private ManageJobsAdapter adapter;
    private List<Job> jobList = new ArrayList<>();
    private List<Job> allJobs = new ArrayList<>();
    private DBHelper dbHelper;
    private int companyId;
    private LinearLayout layoutEmpty;
    private ExtendedFloatingActionButton fabAddJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_jobs);

        // Sử dụng Singleton DBHelper
        dbHelper = DBHelper.getInstance(this);

        UserSession session=new UserSession(this);int userId=session.isLoggedIn()?session.getUserId():-1;User user=userId>0?dbHelper.getUserById(userId):null;
        if(user==null||!DBHelper.ROLE_EMPLOYER.equals(user.getRole())||!DBHelper.STATUS_ACTIVE.equals(user.getStatus())){session.clear();Intent i=new Intent(this,LoginActivity.class);i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);startActivity(i);finish();return;}
        Company company = dbHelper.getCompanyByUserId(userId);
        if (company != null) companyId = company.getId();

        initViews();
        EditText search=findViewById(R.id.etJobSearch); if(search!=null) search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){}public void onTextChanged(CharSequence s,int a,int b,int c){filter(s.toString());}public void afterTextChanged(Editable e){}});
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
                            if (dbHelper.deleteJobSafely(job.getId(), companyId)) {
                                Toast.makeText(ManageJobsActivity.this, "Đã xóa tin", Toast.LENGTH_SHORT).show();
                                loadJobs();
                            } else Toast.makeText(ManageJobsActivity.this,"Tin đã có ứng viên hoặc không thuộc công ty của bạn; đã giữ nguyên dữ liệu.",Toast.LENGTH_LONG).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        rvManageJobs.setAdapter(adapter);
    }

    private void loadJobs() {
        jobList.clear();
        allJobs.clear(); allJobs.addAll(dbHelper.getJobsByCompanyId(companyId)); jobList.addAll(allJobs);
        adapter.notifyDataSetChanged();

        if (jobList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvManageJobs.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvManageJobs.setVisibility(View.VISIBLE);
        }
    }
    private void filter(String q){jobList.clear();String x=q==null?q.trim().toLowerCase(java.util.Locale.ROOT):q.trim().toLowerCase(java.util.Locale.ROOT);for(Job j:allJobs)if(x.isEmpty()||(j.getTitle()!=null&&j.getTitle().toLowerCase(java.util.Locale.ROOT).contains(x))||(j.getLocation()!=null&&j.getLocation().toLowerCase(java.util.Locale.ROOT).contains(x)))jobList.add(j);adapter.notifyDataSetChanged();layoutEmpty.setVisibility(jobList.isEmpty()?View.VISIBLE:View.GONE);rvManageJobs.setVisibility(jobList.isEmpty()?View.GONE:View.VISIBLE);}

    @Override
    protected void onResume() {
        super.onResume();
        loadJobs();
    }
}
