package com.example.jobfinderapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobfinderapp.models.Job;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    // Interface cho nút Favorite
    public interface OnFavoriteClickListener {
        void onFavoriteClick(Job job);
    }

    private Context context;
    private List<Job> jobList;
    private int userId;
    private OnFavoriteClickListener favoriteClickListener;

    // 1. Constructor 2 tham số đơn giản
    public JobAdapter(Context context, List<Job> jobList) {
        this.context = context;
        this.jobList = jobList;
    }

    // 2. Constructor 4 tham số đầy đủ
    public JobAdapter(Context context, List<Job> jobList, int userId, OnFavoriteClickListener listener) {
        this.context = context;
        this.jobList = jobList;
        this.userId = userId;
        this.favoriteClickListener = listener;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        if (job == null) return;

        if (holder.tvJobTitle != null) holder.tvJobTitle.setText(job.getTitle());
        if (holder.tvCompanyName != null) holder.tvCompanyName.setText(job.getCompanyName());
        if (holder.tvSalary != null) holder.tvSalary.setText(job.getSalary());
        if (holder.tvLocation != null) holder.tvLocation.setText(job.getLocation());

        if (holder.ivCompanyLogo != null) {
            Glide.with(holder.itemView.getContext())
                    .load(job.getCompanyLogo())
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivCompanyLogo);
        }

        if (holder.ivFavorite != null) {
            holder.ivFavorite.setOnClickListener(v -> {
                if (favoriteClickListener != null) {
                    favoriteClickListener.onFavoriteClick(job);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCompanyLogo, ivFavorite;
        TextView tvJobTitle, tvCompanyName, tvSalary, tvLocation;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCompanyLogo = itemView.findViewById(R.id.ivCompanyLogo);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }
}