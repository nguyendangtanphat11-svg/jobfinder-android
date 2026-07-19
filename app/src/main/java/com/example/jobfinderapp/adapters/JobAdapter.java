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
import com.example.jobfinderapp.activities.JobDetailActivity;
import com.example.jobfinderapp.database.DBHelper;
import com.example.jobfinderapp.models.Job;

import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList;
    private int currentUserId;
    private DBHelper dbHelper;
    private OnFavoriteClickListener favoriteClickListener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Job job, boolean isFavorite);
    }

    public JobAdapter(Context context, List<Job> jobList, int currentUserId, OnFavoriteClickListener favoriteClickListener) {
        this.context = context;
        this.jobList = jobList;
        this.currentUserId = currentUserId;
        this.dbHelper = new DBHelper(context);
        this.favoriteClickListener = favoriteClickListener;
    }

    public void updateList(List<Job> newList) {
        this.jobList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        holder.tvJobTitle.setText(job.getTitle());
        holder.tvCompanyName.setText(job.getCompanyName());
        holder.tvSalary.setText(job.getSalary());
        holder.tvLocation.setText(job.getLocation());
        holder.tvDeadline.setText("Hạn: " + job.getDeadline());

        // Kiểm tra trạng thái favorite từ SQLite
        boolean isFav = dbHelper.isFavorite(currentUserId, job.getId());
        if (isFav) {
            holder.ivFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.ivFavorite.setImageResource(android.R.drawable.btn_star_big_off);
        }

        Glide.with(context)
                .load(job.getCompanyLogo())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.ivCompanyLogo);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetailActivity.class);
            intent.putExtra("JOB_ID", job.getId());
            context.startActivity(intent);
        });

        holder.ivFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(job, isFav);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCompanyLogo, ivFavorite;
        TextView tvJobTitle, tvCompanyName, tvSalary, tvLocation, tvDeadline;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCompanyLogo = itemView.findViewById(R.id.ivCompanyLogo);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
        }
    }
}
