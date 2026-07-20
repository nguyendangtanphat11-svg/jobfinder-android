package com.example.jobfinderapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinderapp.R;
import com.example.jobfinderapp.models.Job;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ManageJobsAdapter extends RecyclerView.Adapter<ManageJobsAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList;
    private OnJobActionListener listener;

    public interface OnJobActionListener {
        void onEdit(Job job);
        void onDelete(Job job);
    }

    public ManageJobsAdapter(Context context, List<Job> jobList, OnJobActionListener listener) {
        this.context = context;
        this.jobList = jobList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.tvJobTitle.setText(job.getTitle());
        holder.tvSalary.setText(job.getSalary());
        holder.tvLocation.setText("📍 " + job.getLocation());
        holder.tvDeadline.setText("Hạn nộp: " + job.getDeadline());
        holder.tvStatusBadge.setText(job.getStatus());

        if ("Đã đóng".equals(job.getStatus())) {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_rejected);
            holder.tvStatusBadge.setTextColor(context.getResources().getColor(R.color.error_color));
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvStatusBadge.setTextColor(context.getResources().getColor(R.color.primary_green));
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(job));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(job));
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvSalary, tvLocation, tvDeadline, tvStatusBadge;
        MaterialButton btnEdit, btnDelete;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            btnEdit = itemView.findViewById(R.id.btnEditJob);
            btnDelete = itemView.findViewById(R.id.btnDeleteJob);
        }
    }
}
