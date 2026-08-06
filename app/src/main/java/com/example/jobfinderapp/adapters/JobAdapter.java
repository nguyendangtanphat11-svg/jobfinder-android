package com.example.jobfinderapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.activities.ApplyActivity;
import com.example.jobfinderapp.activities.JobDetailActivity;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Job;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {
    public interface OnFavoriteClickListener { void onFavoriteClick(Job job, boolean isFavorite); }

    private final Context context;
    private final int currentUserId;
    private final DBHelper dbHelper;
    private final OnFavoriteClickListener favoriteClickListener;
    private List<Job> jobList = new ArrayList<>();

    public JobAdapter(Context context, int currentUserId, OnFavoriteClickListener listener) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.favoriteClickListener = listener;
        this.dbHelper = DBHelper.getInstance(context);
    }

    public void updateList(List<Job> newList) {
        jobList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull @Override public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new JobViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.tvJobTitle.setText(job.getTitle());
        holder.tvCompanyName.setText(valueOrFallback(job.getCompanyName(), "Công ty đang cập nhật"));
        holder.tvSalary.setText(valueOrFallback(job.getSalary(), "Thỏa thuận"));
        holder.tvLocation.setText(valueOrFallback(job.getLocation(), "Linh hoạt"));
        holder.tvJobType.setText(valueOrFallback(job.getJobType(), "Toàn thời gian"));
        holder.tvDeadline.setText("Hạn: " + valueOrFallback(job.getDeadline(), "Đang cập nhật"));
        boolean isFavorite = dbHelper.isFavorite(currentUserId, job.getId());
        holder.ivFavorite.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        holder.ivFavorite.setContentDescription(isFavorite ? "Bỏ lưu việc làm" : "Lưu việc làm");
Glide.with(context).load(job.getCompanyLogo()).placeholder(R.drawable.ic_default_company).error(R.drawable.ic_default_company).into(holder.ivCompanyLogo);
        holder.itemView.setOnClickListener(v -> openDetail(job));
        holder.ivFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) favoriteClickListener.onFavoriteClick(job, isFavorite);
        });
        holder.btnApplyFast.setOnClickListener(v -> {
            Intent intent = new Intent(context, ApplyActivity.class);
            intent.putExtra("JOB_ID", job.getId());
            intent.putExtra("JOB_TITLE_KEY", job.getTitle());
            context.startActivity(intent);
        });
    }

    private void openDetail(Job job) {
        Intent intent = new Intent(context, JobDetailActivity.class);
        intent.putExtra("JOB_ID", job.getId());
        context.startActivity(intent);
    }
    private String valueOrFallback(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value; }
    @Override public int getItemCount() { return jobList.size(); }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivCompanyLogo, ivFavorite;
        final TextView tvJobTitle, tvCompanyName, tvSalary, tvLocation, tvJobType, tvDeadline;
        final MaterialButton btnApplyFast;
        JobViewHolder(@NonNull View view) {
            super(view);
            ivCompanyLogo = view.findViewById(R.id.ivCompanyLogo); ivFavorite = view.findViewById(R.id.ivFavorite);
            tvJobTitle = view.findViewById(R.id.tvJobTitle); tvCompanyName = view.findViewById(R.id.tvCompanyName);
            tvSalary = view.findViewById(R.id.tvSalary); tvLocation = view.findViewById(R.id.tvLocation);
            tvJobType = view.findViewById(R.id.tvJobType); tvDeadline = view.findViewById(R.id.tvDeadline);
            btnApplyFast = view.findViewById(R.id.btnApplyFast);
        }
    }
}
