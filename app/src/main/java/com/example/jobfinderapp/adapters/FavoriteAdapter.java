package com.example.jobfinderapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinderapp.R;
import com.example.jobfinderapp.activities.JobDetailActivity;
import com.example.jobfinderapp.models.Job;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    public interface OnRemoveFavoriteListener {
        void onRemoveFavorite(Job job);
    }

    private final Context context;
    private final List<Job> jobList = new ArrayList<>();
    private final OnRemoveFavoriteListener removeFavoriteListener;

    public FavoriteAdapter(Context context, List<Job> jobs, OnRemoveFavoriteListener listener) {
        this.context = context;
        this.removeFavoriteListener = listener;
        updateList(jobs);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_favorite_job, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.tvJobTitle.setText(valueOrFallback(job.getTitle(), context.getString(R.string.application_job_unavailable)));
        holder.tvCompanyName.setText(valueOrFallback(job.getCompanyName(), context.getString(R.string.application_company_unavailable)));
        holder.tvSalary.setText(valueOrFallback(job.getSalary(), context.getString(R.string.not_updated)));
        holder.tvLocation.setText(valueOrFallback(job.getLocation(), context.getString(R.string.not_updated)));
        holder.tvJobType.setText(valueOrFallback(job.getJobType(), context.getString(R.string.not_updated)));
        holder.tvDeadline.setText(context.getString(R.string.favorite_deadline_format,
                valueOrFallback(job.getDeadline(), context.getString(R.string.not_updated))));

        boolean recruiting = "Đang tuyển".equals(job.getStatus());
        holder.tvJobStatus.setText(recruiting ? R.string.favorite_status_recruiting : R.string.favorite_status_closed);
        holder.tvJobStatus.setBackgroundResource(recruiting
                ? R.drawable.bg_application_status_waiting : R.drawable.bg_application_status_rejected);
        holder.tvJobStatus.setTextColor(context.getColor(recruiting
                ? R.color.status_sent_text : R.color.status_rejected_text));

Glide.with(context).load(job.getCompanyLogo()).placeholder(R.drawable.ic_default_company)
.error(R.drawable.ic_default_company).into(holder.imgCompanyLogo);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetailActivity.class);
            intent.putExtra("JOB_ID", job.getId());
            context.startActivity(intent);
        });
        holder.btnRemoveFavorite.setOnClickListener(v -> {
            if (removeFavoriteListener != null) removeFavoriteListener.onRemoveFavorite(job);
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void updateList(List<Job> newList) {
        jobList.clear();
        if (newList != null) jobList.addAll(newList);
        notifyDataSetChanged();
    }

    public void removeByJobId(int jobId) {
        for (int index = 0; index < jobList.size(); index++) {
            if (jobList.get(index).getId() == jobId) {
                jobList.remove(index);
                notifyItemRemoved(index);
                return;
            }
        }
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ShapeableImageView imgCompanyLogo;
        final TextView tvJobTitle, tvCompanyName, tvSalary, tvLocation, tvJobType, tvDeadline, tvJobStatus;
        final ImageButton btnRemoveFavorite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCompanyLogo = itemView.findViewById(R.id.imgCompanyLogo);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvJobType = itemView.findViewById(R.id.tvJobType);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvJobStatus = itemView.findViewById(R.id.tvJobStatus);
            btnRemoveFavorite = itemView.findViewById(R.id.btnRemoveFavorite);
        }
    }
}
