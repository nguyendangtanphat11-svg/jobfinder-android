package com.example.jobfinderapp;

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

public class    JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private List<Job> jobList;

    public JobAdapter(List<Job> jobList) {
        this.jobList = jobList;
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

        // Gán dữ liệu chữ
        holder.tvTitle.setText(job.getTitle());
        holder.tvCompany.setText(job.getCompanyName());
        holder.tvSalary.setText(job.getSalary());
        holder.tvLocation.setText(job.getLocation());

        // XỬ LÝ ĐỔ ẢNH ĐỘNG TỪ URL
        Glide.with(holder.itemView.getContext())
                .load(job.getCompanyLogo())
                .placeholder(android.R.color.darker_gray)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.imgCompany);
    }

    @Override
    public int getItemCount() {
        return jobList != null ? jobList.size() : 0;
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCompany, tvSalary, tvLocation;
        ImageView imgCompany;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompany = itemView.findViewById(R.id.tvCompanyName);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            imgCompany = itemView.findViewById(R.id.ivCompanyLogo);
        }
    }
}
